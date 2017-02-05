package ca.rmen.android.poetassistant.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * https://code.google.com/p/android/issues/detail?id=23381
 */
public class CABEditText extends AppCompatEditText implements HackFor23381 {

    public interface ImeListener {
        /**
         * The soft keyboard was just closed.
         */
        void onImeClosed();
    }

    private boolean shouldWindowFocusWait;
    private ImeListener mImeListener;

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

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (mImeListener != null
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP) {
            mImeListener.onImeClosed();
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setImeListener(ImeListener listener) {
        mImeListener = listener;
    }

}
