package ca.rmen.android.poetassistant.main.common.ui.graphics

import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import kotlin.math.roundToInt

/**
 * A Compose [Painter] that can draw an Android [Drawable].
 *
 * This is useful for displaying app icons (which are Drawables) in Compose UI.
 *
 * @param drawable The Drawable to be painted.
 */
// From https://github.com/BoD/a/blob/master/app/src/main/kotlin/org/jraf/android/a/ui/main/MainLayout.kt
class DrawablePainter(private val drawable: Drawable) : Painter() {
    override val intrinsicSize: Size =
        Size(width = drawable.intrinsicWidth.toFloat(), height = drawable.intrinsicHeight.toFloat())

    override fun DrawScope.onDraw() {
        drawIntoCanvas { canvas ->
            // Update the Drawable's bounds to match the current size
            drawable.setBounds(0, 0, size.width.roundToInt(), size.height.roundToInt())
            drawable.draw(canvas.nativeCanvas)
        }
    }
}