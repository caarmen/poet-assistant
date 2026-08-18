package ca.rmen.android.poetassistant.main.favorites

import android.graphics.drawable.Drawable

/**
 * Represents an external app that supports the ACTION_PROCESS_TEXT intent.
 * These apps can be shown as menu items for processing text.
 *
 * @param label The display name of the app.
 * @param icon The app's icon drawable.
 * @param packageName The package name of the app.
 * @param className The class name of the activity that handles the intent.
 */
data class ExternalAppMenuItem(
    val label: String,
    val icon: Drawable,
    val packageName: String,
    val className: String,
)
