package ca.rmen.android.poetassistant.settings

enum class Theme {
    LIGHT,
    DARK,
    AUTO,
}
enum class Layout {
    CLEAN,
    EFFICIENT
}

data class Settings(
    val layout: Layout,
    val theme: Theme,
    // TODO add more settings progressively
)
