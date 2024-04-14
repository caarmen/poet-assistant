package ca.rmen.android.poetassistant.shadows;

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowActivity
import org.robolectric.util.ReflectionHelpers

@Implements(Activity::class)
class MyShadowActivity : ShadowActivity() {
    var previousActivity: MyShadowActivity? = null
    var scenario: ActivityScenario<Activity>? = null

    // We need access to the controller field, but it's private in the super class.
    // We'll make our own copy of the field in this class, and assign it when
    // attachController is called.
    private var controller: ActivityController<*>? = null

    override fun <T : Activity?> attachController(controller: ActivityController<*>?) {
        super.attachController<T>(controller)
        this.controller = controller
    }

    @Implementation
    fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        // Call through to the actual Activity.startActivityForResult()
        Shadow.directlyOn<Unit>(
            realActivity, Activity::class.java.name, "startActivityForResult",
            ReflectionHelpers.ClassParameter(Intent::class.java, intent),
            ReflectionHelpers.ClassParameter(Int::class.java, requestCode),
            ReflectionHelpers.ClassParameter(Bundle::class.java, options),
        )
        // We need to pause this activity when launching the next activity
        controller?.pause()
    }

    private var isFinishing = false

    @Implementation
    public override fun finish() {
        super.finish()

        // If a previous activity started us, we should resume
        // that previous activity, now that we're finishing.
        previousActivity?.let {
            it.controller?.resume()
            it.receiveResult(realActivity.intent, resultCode, resultIntent)
        }
        // Close the scenario to free up resources.
        // Need a variable isFinishing to avoid an infinite loop, as
        // scenario.close() will trigger a call to finish().
        if (!isFinishing) {
            isFinishing = true
            scenario?.close()
        }
    }
}
