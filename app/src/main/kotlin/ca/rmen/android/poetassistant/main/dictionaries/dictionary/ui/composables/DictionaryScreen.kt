/*
 * Copyright (c) 2016-present Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.main.dictionaries.dictionary.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.rmen.android.poetassistant.main.common.usecases.OpenExternalSearchUseCase
import ca.rmen.android.poetassistant.main.common.usecases.ShareUseCase
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryScreenState
import ca.rmen.android.poetassistant.main.dictionaries.dictionary.DictionaryScreenViewModel

/**
 * Top-level composable for the Dictionary screen.
 * Observes the ViewModel state and displays the appropriate UI.
 *
 * @param viewModel The DictionaryScreenViewModel.
 * @param shareUseCase Use case for sharing content.
 * @param openExternalSearchUseCase Use case for opening external search.
 * @param modifier The modifier for this composable.
 */
@Composable
fun DictionaryScreen(
    viewModel: DictionaryScreenViewModel,
    shareUseCase: ShareUseCase,
    openExternalSearchUseCase: OpenExternalSearchUseCase,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val shareState by viewModel.share.collectAsStateWithLifecycle()

    LaunchedEffect(shareState) {
        shareState?.let {
            shareUseCase(it, context)
            viewModel.onShareSent()
        }
    }

    DictionaryScreenContent(
        state = state,
        onToggleFavorite = { word, isFavorite ->
            viewModel.onSetFavorite(word, isFavorite)
        },
        onSpeakWord = { word ->
            viewModel.onSpeakWord(word)
        },
        onSearchWeb = { word ->
            openExternalSearchUseCase(word, context)
        },
        modifier = modifier,
    )
}
