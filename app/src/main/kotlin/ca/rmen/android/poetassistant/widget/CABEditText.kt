package ca.rmen.android.poetassistant.widget

import android.content.Context
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.KeyEvent

/**
 * https://code.google.com/p/android/issues/detail?id=23381
 */
class CABEditText : AppCompatEditText, HackFor23381 {

    interface ImeListener {
        /**
         * The soft keyboard was just closed.
         */
        fun onImeClosed()
    }

    constructor(context : Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    constructor(context : Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    private var shouldWindowFocusWait: Boolean = false
    var imeListener: ImeListener? = null

    override fun setWindowFocusWait(shouldWindowFocusWait: Boolean) {
        this.shouldWindowFocusWait = shouldWindowFocusWait
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        if (shouldWindowFocusWait) {
            super.onWindowFocusChanged(hasWindowFocus)
        }
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (imeListener != null
                && event.keyCode == KeyEvent.KEYCODE_BACK
                && event.action == KeyEvent.ACTION_UP) {
            imeListener?.onImeClosed()
        }

        return super.onKeyPreIme(keyCode, event)
    }

}
