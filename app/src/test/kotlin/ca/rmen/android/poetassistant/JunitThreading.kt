package ca.rmen.android.poetassistant

class JunitThreading : Threading {
    override fun executeForeground(body: () -> Unit) {
        body.invoke()
    }

    override fun <T> execute(backgroundTask: () -> T, foregroundTask: ((T) -> Unit)?, errorTask: ((Throwable) -> Unit)?) {
        try {
            val result = backgroundTask.invoke()
            foregroundTask?.invoke(result)
        } catch (t: Throwable) {
            errorTask?.invoke(t)
        }
    }
}