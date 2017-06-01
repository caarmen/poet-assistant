/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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
package ca.rmen.android.poetassistant.widget;

import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public final class ViewShownCompletable {

    private ViewShownCompletable() {
        // prevent instantiation
    }

    // Issue #19: In a specific scenario, the fragments may not be "ready" yet (onCreateView() may not have been called).
    // Wait until the ViewPager is laid out before invoking anything on the fragments.
    // (We assume that the fragments are "ready" once the ViewPager is laid out.)
    public static Completable create(View view) {
        return Completable.create(completableEmitter -> {
            if (view.isShown()) {
                completableEmitter.onComplete();
            } else {
                ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        removeOnGlobalLayoutListener(view, this);
                        view.post(completableEmitter::onComplete);
                    }
                };
                view.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
                completableEmitter.setCancellable(() -> removeOnGlobalLayoutListener(view, onGlobalLayoutListener));
            }
        }).observeOn(AndroidSchedulers.mainThread());
    }

    private static void removeOnGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        } else {
            //noinspection deprecation
            view.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        }
    }
}
