// https://gist.github.com/trobalik/b812e2a4d36edcf4157c279b143c8de1
package ca.rmen.android.poetassistant.main.rules;

import android.util.Log;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Locale;

/**
 * A JUnit {@link TestRule} that implements logic to try a test any number of times before giving up and allowing it to fail.
 */
public class RetryTestRule implements TestRule {

    private static final String TAG = RetryTestRule.class.getSimpleName();

    private final int mRetryCount;

    public RetryTestRule() {
        this(3);
    }
    public RetryTestRule(int retryCount) {
        mRetryCount = retryCount;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new RetryStatement(base, description, mRetryCount);
    }

    private static class RetryStatement extends Statement {

        private final Statement mBase;
        private final Description mDescription;
        private final int mRetryCount;

        private RetryStatement(Statement base, Description description, int retryCount) {
            mBase = base;
            mDescription = description;
            mRetryCount = retryCount;
        }

        @Override
        public void evaluate() throws Throwable {
            Throwable testError = null;
            int numFails = 0;

            for (int i = 0; i < mRetryCount; i++) {
                try {
                    mBase.evaluate();
                    Log.d(TAG, String.format(Locale.US, "Out of %d runs, %d failed", i + 1, numFails));
                    return;
                } catch (Throwable t) {
                    Log.e(TAG, String.format(Locale.US, "%s: run %d failed", mDescription.getDisplayName(), i + 1));
                    testError = t;
                    numFails++;
                }
            }

            Log.e(TAG, String.format(Locale.US, "%s: giving up after %d failures", mDescription.getDisplayName(), mRetryCount));
            assert testError != null;
            throw testError;
        }
    }
}
