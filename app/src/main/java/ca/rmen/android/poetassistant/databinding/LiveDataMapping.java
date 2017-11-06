/*
 * Copyright (c) 2017 Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.databinding;

import android.arch.lifecycle.DefaultLifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public final class LiveDataMapping {
    private LiveDataMapping() {
        // prevent instantiation
    }

    public static LiveData<String> fromObservableField(ObservableField<String> observableField) {
        MutableLiveData<String> liveData = new MutableLiveData<>();
        observableField.addOnPropertyChangedCallback(new BindingCallbackAdapter(() -> liveData.setValue(observableField.get())));
        return liveData;
    }

    /**
     * Observe the given LiveData source, but only be notified per the given debounce timeout.
     */
    public static <T> void debounceObserve(LiveData<T> source, LifecycleOwner owner, Observer<T> liveDataObserver,
                                           long timeout, TimeUnit timeUnit) {
        // Create an Rx observer which will notify the given LiveData observer of the source changes,
        // but subject to the debounce constraints.
        Disposable rxObservable = Observable.create((ObservableEmitter<T> emitter) -> {
            Observer<T> debounceLiveDataObserver = emitter::onNext;
            source.observe(owner, debounceLiveDataObserver);
            emitter.setCancellable(() -> source.removeObserver(debounceLiveDataObserver));
        })
                .debounce(timeout, timeUnit)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(liveDataObserver::onChanged);

        // When the activity is destroyed, we have to dispose of our Rx observer
        owner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                rxObservable.dispose();
                owner.getLifecycle().removeObserver(this);
            }
        });
    }
}
