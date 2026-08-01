// https://gist.github.com/trobalik/b812e2a4d36edcf4157c279b143c8de1
package ca.rmen.android.poetassistant.main.rules

import android.util.Log
import org.junit.Assert.assertNotNull
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.Locale

/**
 * A JUnit {@link TestRule} that implements logic to try a test any number of times before giving up and allowing it to fail.
 */
class RetryTestRule(private val retryCount: Int = 3) : TestRule {

    companion object {
        private val TAG = RetryTestRule::class.simpleName
    }

    override fun apply(base: Statement, description: Description): Statement {
        return RetryStatement(base, description, retryCount)
    }

    private class RetryStatement(
        private val base: Statement,
        private val description: Description,
        private val retryCount: Int
    ) : Statement() {

        override fun evaluate() {
            var testError: Throwable? = null
            var numFails = 0

            for (i in 0 until retryCount) {
                try {
                    base.evaluate()
                    Log.d(TAG, String.format(Locale.US, "Out of %d runs, %d failed", i + 1, numFails))
                    return
                } catch (t: Throwable) {
                    Log.e(TAG, String.format(Locale.US, "%s: run %d failed", description.displayName, i + 1))
                    testError = t
                    numFails++
                }
            }

            Log.e(TAG, String.format(Locale.US, "%s: giving up after %d failures", description.displayName, retryCount))
            assertNotNull(testError)
            throw testError!!
        }
    }
}
