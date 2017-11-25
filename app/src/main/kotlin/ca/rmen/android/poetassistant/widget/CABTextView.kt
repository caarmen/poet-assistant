package ca.rmen.android.poetassistant.widget

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet

/**
 * https://code.google.com/p/android/issues/detail?id=23381
 */
class CABTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), HackFor23381 {
    private var shouldWindowFocusWait: Boolean = false

    override fun setWindowFocusWait(shouldWindowFocusWait: Boolean) {
        this.shouldWindowFocusWait = shouldWindowFocusWait
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        if (!shouldWindowFocusWait) {
            super.onWindowFocusChanged(hasWindowFocus)
        }
    }
}
