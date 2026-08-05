import android.os.Handler
import android.os.Looper
import androidx.test.espresso.idling.CountingIdlingResource

fun CountingIdlingResource.decrementDelayed(delay: Long) {
    Handler(Looper.getMainLooper()).postDelayed({ decrement() }, delay)
}
