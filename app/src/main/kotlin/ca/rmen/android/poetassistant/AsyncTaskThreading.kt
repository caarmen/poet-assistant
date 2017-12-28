package ca.rmen.android.poetassistant

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log

open class AsyncTaskThreading : Threading {

    companion object {
        private val TAG = Constants.TAG + AsyncTaskThreading::class.java.simpleName
    }

    override fun executeForeground(body: () -> Unit) {
        Handler(Looper.getMainLooper()).post({ body.invoke() })
    }

    override fun <T> execute(backgroundTask: () -> T, foregroundTask: ((T) -> Unit)?, errorTask: ((Throwable) -> Unit)?) {
        AsyncTask.execute({
            try {
                val result = backgroundTask.invoke()
                if (foregroundTask != null) {
                    Handler(Looper.getMainLooper()).post({ foregroundTask.invoke(result) })
                }
            } catch (t: Throwable) {
                Log.v(TAG, "Error running background task", t)
                if (errorTask != null) {
                    Handler(Looper.getMainLooper()).post({ errorTask.invoke(t) })
                }
            }
        })
    }
}