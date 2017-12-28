package ca.rmen.android.poetassistant

interface Threading {

    fun executeForeground(body: () -> Unit)

    fun <T> execute(backgroundTask: () -> T, foregroundTask: ((T) -> Unit)? = null, errorTask: ((Throwable) -> Unit)? = null)
}