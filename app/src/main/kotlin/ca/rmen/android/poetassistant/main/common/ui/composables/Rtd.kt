package ca.rmen.android.poetassistant.main.common.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.Tab

const val ITEM_RHYMER_TAG = "ItemRhymer_"
const val ITEM_THESAURUS_TAG = "ItemThesaurus_"
const val ITEM_DICTIONARY_TAG = "ItemDictionary_"

/**
 * The R/T/D icons to open a word in the rhymer, thesaurus and dictionary
 * tabs.
 *
 * @param word The word to open in the other tabs.
 * @param hasDefinition Whether the word has a dictionary definition. When
 * false, the thesaurus and dictionary icons are dimmed (Material 3
 * disabled alpha) and not clickable; the rhymer icon is unaffected.
 * @param onSearchInTab Callback to request opening the word in a tab.
 */
@Composable
fun Rtd(
    word: String,
    hasDefinition: Boolean = true,
    onSearchInTab: (Tab) -> Unit,
) {
    Image(
        painter = painterResource(R.drawable.ic_rhymer),
        contentDescription = stringResource(R.string.tab_rhymer),
        modifier = Modifier
            .size(44.dp)
            .padding(horizontal = 4.dp)
            .clickable(onClick = { onSearchInTab(Tab.RHYMER) })
            .testTag("$ITEM_RHYMER_TAG$word"),
    )
    Image(
        painter = painterResource(R.drawable.ic_thesaurus),
        contentDescription = stringResource(R.string.tab_thesaurus),
        modifier = Modifier
            .size(44.dp)
            .padding(horizontal = 4.dp)
            .dimOrClickable(hasDefinition) { onSearchInTab(Tab.THESAURUS) }
            .testTag("$ITEM_THESAURUS_TAG$word"),
    )
    Image(
        painter = painterResource(R.drawable.ic_dictionary),
        contentDescription = stringResource(R.string.tab_dictionary),
        modifier = Modifier
            .size(44.dp)
            .padding(horizontal = 4.dp)
            .dimOrClickable(hasDefinition) { onSearchInTab(Tab.DICTIONARY) }
            .testTag("$ITEM_DICTIONARY_TAG$word"),
    )
}

/**
 * Clickable when the word has a definition; dimmed to the Material 3
 * disabled alpha otherwise.
 */
private fun Modifier.dimOrClickable(hasDefinition: Boolean, onClick: () -> Unit): Modifier =
    if (hasDefinition) clickable(onClick = onClick) else alpha(0.38f)
