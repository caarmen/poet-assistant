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

package ca.rmen.android.poetassistant.main.reader.ui.composables

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.compat.HtmlCompat

/**
 * The word count help dialog. Explains which characters do not break
 * up words. The help message contains HTML (bold text, line breaks):
 * it is parsed with the project HtmlCompat and mapped to an
 * AnnotatedString (bold spans, line breaks).
 *
 * @param onDismiss Called when the dialog is dismissed.
 */
@Composable
fun ReaderWordCountHelpDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.word_count_help_title)) },
        text = { Text(text = rememberWordCountHelpMessage()) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
    )
}

/**
 * The help message, parsed from HTML and mapped to an AnnotatedString:
 * bold spans become bold text, line breaks become newlines.
 */
@Composable
private fun rememberWordCountHelpMessage(): AnnotatedString {
    val context = LocalContext.current
    return remember {
        val spanned = HtmlCompat.fromHtml(context.getString(R.string.word_count_help_message)) as Spanned
        buildAnnotatedString {
            append(spanned.toString())
            spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
                if (span is StyleSpan && span.style == Typeface.BOLD) {
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        spanned.getSpanStart(span),
                        spanned.getSpanEnd(span),
                    )
                }
            }
        }
    }
}
