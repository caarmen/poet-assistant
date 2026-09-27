package ca.rmen.android.poetassistant.main.wotd.ui.composables.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.rmen.android.poetassistant.main.wotd.ui.composables.WotdHeader
import ca.rmen.android.poetassistant.theme.AppTheme

@Composable
@Preview(showBackground = true)
fun WotdHeaderPreview() {
    AppTheme {
        WotdHeader()
    }
}
