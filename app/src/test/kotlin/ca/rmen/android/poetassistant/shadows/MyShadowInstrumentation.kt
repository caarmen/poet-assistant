package ca.rmen.android.poetassistant.shadows

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.test.core.app.ActivityScenario
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowInstrumentation

@Implements(value = Instrumentation::class, looseSignatures = true)
class MyShadowInstrumentation : ShadowInstrumentation() {
    @Implementation
    override fun execStartActivity(
        who: Context?,
        contextThread: IBinder?,
        token: IBinder?,
        target: Activity?,
        intent: Intent?,
        requestCode: Int,
        options: Bundle?
    ): Instrumentation.ActivityResult? {
        val result = super.execStartActivity(who, contextThread, token, target, intent, requestCode, options)
        if (intent != null) {
            try {
                // Actually start the next activity.
                val nextActivityScenario =
                    if (requestCode >= 0) ActivityScenario.launchActivityForResult(intent) else ActivityScenario.launch<Activity>(
                        intent
                    )
                // Prepare cleanup for when this next activity will finish.
                // Set its reference to its scenario and previousActivity, so
                // it can clean them up in its finish() method.
                nextActivityScenario.onActivity {
                    val shadow: MyShadowActivity = Shadow.extract(it)
                    shadow.scenario = nextActivityScenario
                    shadow.previousActivity = Shadow.extract(target)
                }
            } catch (t: Throwable) {
                println("Error launching activity ${t.message}")
            }
        }
        return result
    }
}