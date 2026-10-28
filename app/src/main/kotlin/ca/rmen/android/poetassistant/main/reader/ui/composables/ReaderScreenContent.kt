package ca.rmen.android.poetassistant.main.reader.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.main.reader.PlayButtonState
import ca.rmen.android.poetassistant.main.reader.ReaderScreenState

/**
 * The reader screen content: the play button, the poem text field,
 * and the word count. Kept separate from [ReaderScreen] so previews
 * can render it with static state.
 *
 * @param state The reader screen state.
 * @param onPoemEdited Called with the new poem text and selection.
 * @param onPlayClicked Called when the play button is clicked.
 * @param onWordCountClicked Called when the word count is clicked.
 * @param modifier The modifier for this composable.
 */
@Composable
fun ReaderScreenContent(
    modifier: Modifier = Modifier,
    state: ReaderScreenState,
    onPoemEdited: (TextFieldValue) -> Unit,
    onPlayClicked: () -> Unit,
    onWordCountClicked: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // The whole screen scrolls, so the end of a long poem
            // stays reachable with the keyboard open.
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
    ) {
        IconButton(
            onClick = onPlayClicked,
            enabled = state.playButtonState != PlayButtonState.PLAY_DISABLED,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .testTag(READER_PLAY_BUTTON_TAG),
        ) {
            Icon(
                painter = painterResource(playIcon(state.playButtonState)),
                contentDescription = stringResource(R.string.tts_play),
                modifier = Modifier.size(48.dp),
            )
        }
        TextField(
            value = state.textFieldValue,
            onValueChange = onPoemEdited,
            placeholder = { Text(stringResource(R.string.tts_hint)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrect = true,
            ),
            // Multi-line: the text field grows with the poem.
            maxLines = Int.MAX_VALUE,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(READER_POEM_TEXT_FIELD_TAG),
        )
        ReaderWordCount(
            wordCount = state.wordCount,
            onClick = onWordCountClicked,
            modifier = Modifier.align(Alignment.Start),
        )
    }
}

private fun playIcon(playButtonState: PlayButtonState) = when (playButtonState) {
    PlayButtonState.PLAY_ENABLED -> R.drawable.ic_play_enabled
    PlayButtonState.PLAY_DISABLED -> R.drawable.ic_play_disabled
    PlayButtonState.STOP -> R.drawable.ic_stop
}
