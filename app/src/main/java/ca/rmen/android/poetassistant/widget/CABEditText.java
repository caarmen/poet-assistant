package ca.rmen.android.poetassistant.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

/**
 * https://code.google.com/p/android/issues/detail?id=23381
 */
public class CABEditText extends AppCompatEditText implements HackFor23381 {
    private boolean shouldWindowFocusWait;

    public CABEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CABEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CABEditText(Context context) {
        super(context);
    }

    @Override
    public void setWindowFocusWait(boolean shouldWindowFocusWait) {
        this.shouldWindowFocusWait = shouldWindowFocusWait;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if(!shouldWindowFocusWait) {
            super.onWindowFocusChanged(hasWindowFocus);
        }
    }
}
