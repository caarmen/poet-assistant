package ca.rmen.android.poetassistant.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * https://code.google.com/p/android/issues/detail?id=23381
 */
public class CABTextView extends AppCompatTextView implements HackFor23381 {
    private boolean shouldWindowFocusWait;

    public CABTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CABTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CABTextView(Context context) {
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
