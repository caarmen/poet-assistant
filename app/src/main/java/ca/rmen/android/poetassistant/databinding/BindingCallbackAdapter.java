package ca.rmen.android.poetassistant.databinding;

import android.databinding.Observable;
import android.support.annotation.NonNull;

// http://tech.vg.no/2016/10/16/two-way-data-binding-without-rxjava/
public class BindingCallbackAdapter extends Observable.OnPropertyChangedCallback {
    private final Callback mCallback;

    public BindingCallbackAdapter(@NonNull BindingCallbackAdapter.Callback callback) {
        mCallback = callback;
    }

    @Override
    public void onPropertyChanged(Observable sender, int propertyId) {
        mCallback.onChanged();
    }

    public interface Callback {
        void onChanged();
    }
}

