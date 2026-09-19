package ca.rmen.android.poetassistant.main.common.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.Tab

const val ITEM_RHYMER_TAG = "ItemRhymer_"
const val ITEM_THESAURUS_TAG = "ItemThesaurus_"
const val ITEM_DICTIONARY_TAG = "ItemDictionary_"

@Composable
fun Rtd(
    word: String,
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
            .clickable(onClick = { onSearchInTab(Tab.THESAURUS) })
            .testTag("$ITEM_THESAURUS_TAG$word"),
    )
    Image(
        painter = painterResource(R.drawable.ic_dictionary),
        contentDescription = stringResource(R.string.tab_dictionary),
        modifier = Modifier
            .size(44.dp)
            .padding(horizontal = 4.dp)
            .clickable(onClick = { onSearchInTab(Tab.DICTIONARY) })
            .testTag("$ITEM_DICTIONARY_TAG$word"),
    )
}
