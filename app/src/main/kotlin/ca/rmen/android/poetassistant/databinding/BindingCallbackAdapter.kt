package ca.rmen.android.poetassistant.databinding

import androidx.databinding.Observable

// http://tech.vg.no/2016/10/16/two-way-data-binding-without-rxjava/
class BindingCallbackAdapter(private val callback: Callback) : Observable.OnPropertyChangedCallback() {

    interface Callback {
        fun onChanged()
    }

    override fun onPropertyChanged(sender: Observable, propertyId: Int) {
        callback.onChanged()
    }
}
