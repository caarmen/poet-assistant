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
    val isThesaurusReverseLookupEnabled: Boolean,
    val isAllRhymesEnabled: Boolean,
    val isAOAAMatchEnabled: Boolean,
    val isAORAOMatchEnabled: Boolean,
    // TODO add more settings progressively
)
