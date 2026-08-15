package ca.rmen.android.poetassistant.main.favorites

/**
 * Data class representing content to be shared.
 * 
 * @param title The title for the share intent.
 * @param content The actual content to be shared.
 */
data class Share(
    val title: String,
    val content: String,
)
