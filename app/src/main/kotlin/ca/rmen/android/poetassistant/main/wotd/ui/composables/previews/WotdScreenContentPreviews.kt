package ca.rmen.android.poetassistant.main.wotd.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.wotd.WotdHistoryItem
import ca.rmen.android.poetassistant.main.wotd.WotdScreenState
import ca.rmen.android.poetassistant.main.wotd.ui.composables.WotdScreenContent
import ca.rmen.android.poetassistant.settings.Layout
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
private fun WotdScreenContentPreviewContent(
    state: WotdScreenState,
    layout: Layout,
) {
    AppTheme {
        WotdScreenContent(
            state = state,
            layout = layout,
            externalAppMenuItemsProducer = { _ -> emptyList() },
            onSetFavorite = { _, _ -> },
            onCopy = {},
            onSearchInTab = { _, _ -> },
            onExternalAppSelected = { _, _ -> }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun WotdScreenContentSuccessPreview() = WotdScreenContentPreviewContent(
    state = WotdScreenState.Success(
        entries = listOf(
            WotdHistoryItem("example", "Sep 27", true),
            WotdHistoryItem("examples", "Sep 26", false),
            WotdHistoryItem("exampler", "Sep 25", false)
        )
    ),
    layout = Layout.EFFICIENT
)

@Composable
@Preview(showBackground = true)
fun WotdScreenContentCleanPreview() = WotdScreenContentPreviewContent(
    state = WotdScreenState.Success(
        entries = List(100) { index ->
            WotdHistoryItem("word$index", "Sep ${index % 28 + 1}", index % 2 == 0)
        }
    ),
    layout = Layout.CLEAN
)

@Composable
@Preview(showBackground = true)
fun WotdScreenContentEmptyPreview() = WotdScreenContentPreviewContent(
    state = WotdScreenState.Success(entries = emptyList()),
    layout = Layout.EFFICIENT
)

@Composable
@Preview(showBackground = true)
fun WotdScreenContentIdlePreview() = WotdScreenContentPreviewContent(
    state = WotdScreenState.Idle,
    layout = Layout.EFFICIENT
)
