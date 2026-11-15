/*
 * Copyright (c) 2016 - present Carmen Alvarez
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
package ca.rmen.android.poetassistant.about

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.ExcludeFromJacocoGeneratedReport
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.theme.AppTheme

// Test tags for the rows, which are defined here.
const val ABOUT_SOURCE_CODE_ROW_TAG = "about_source_code"
const val ABOUT_BUG_REPORT_ROW_TAG = "about_bug_report"
const val ABOUT_RATE_ROW_TAG = "about_rate"
const val ABOUT_LEGAL_ROW_TAG = "about_legal"
const val ABOUT_PRIVACY_POLICY_ROW_TAG = "about_privacy_policy"
const val ABOUT_LICENSE_APP_ROW_TAG = "about_license_app"
const val ABOUT_LICENSE_RHYMER_ROW_TAG = "about_license_rhymer"
const val ABOUT_LICENSE_THESAURUS_ROW_TAG = "about_license_thesaurus"
const val ABOUT_LICENSE_DICTIONARY_ROW_TAG = "about_license_dictionary"
const val ABOUT_LICENSE_GOOGLE_NGRAM_ROW_TAG = "about_license_google_ngram"
const val ABOUT_LICENSE_SUPPORT_TAG = "about_license_support_lib"
const val ABOUT_LICENSE_DAGGER_TAG = "about_license_dagger"
const val ABOUT_LICENSE_KOTLIN_TAG = "about_license_kotlin"
const val ABOUT_LICENSE_MATERIAL_TAG = "about_license_material"
const val ABOUT_LICENSE_PORTER_STEMMER_TAG = "about_license_porter_stemmer"

private val URL_REGEX = Regex("""https?://[^\s]+""")
private val EMAIL_REGEX = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")

/** A link row of the about screen: an icon followed by text. */
private data class AboutLinkEntry(
    @DrawableRes val iconRes: Int,
    @StringRes val textRes: Int,
    val testTag: String,
    val isSubItem: Boolean = false,
)

/** A clickable license row, opening a license asset in [LicenseActivity]. */
private data class LicenseEntry(
    @StringRes val labelRes: Int,
    @StringRes val titleRes: Int,
    val assetFile: String,
    val testTag: String,
)

private val TOP_LINK_ENTRIES = listOf(
    AboutLinkEntry(R.drawable.ic_source_code, R.string.about_projectUrl, ABOUT_SOURCE_CODE_ROW_TAG),
    AboutLinkEntry(R.drawable.ic_bug_report_24dp, R.string.about_issuesUrl, ABOUT_BUG_REPORT_ROW_TAG),
    AboutLinkEntry(R.drawable.ic_rate, R.string.about_rateUrl, ABOUT_RATE_ROW_TAG),
    AboutLinkEntry(R.drawable.ic_legal, R.string.about_legal, ABOUT_LEGAL_ROW_TAG),
)

private val LICENSE_ENTRIES = listOf(
    LicenseEntry(R.string.about_license_app, R.string.app_name, "LICENSE.txt", ABOUT_LICENSE_APP_ROW_TAG),
    LicenseEntry(
        R.string.about_license_rhyming_dictionary,
        R.string.about_license_rhyming_dictionary,
        "LICENSE-rhyming-dictionary.txt",
        ABOUT_LICENSE_RHYMER_ROW_TAG,
    ),
    LicenseEntry(
        R.string.about_license_thesaurus,
        R.string.about_license_thesaurus,
        "LICENSE-thesaurus-wordnet.txt",
        ABOUT_LICENSE_THESAURUS_ROW_TAG,
    ),
    LicenseEntry(
        R.string.about_license_dictionary,
        R.string.about_license_dictionary,
        "LICENSE-dictionary-wordnet.txt",
        ABOUT_LICENSE_DICTIONARY_ROW_TAG,
    ),
    LicenseEntry(
        R.string.about_license_google_ngram_dataset,
        R.string.about_license_google_ngram_dataset,
        "LICENSE-google-ngram-dataset.txt",
        ABOUT_LICENSE_GOOGLE_NGRAM_ROW_TAG,
    ),
)

private val PRIVACY_POLICY_ENTRY =
    AboutLinkEntry(R.drawable.ic_bullet, R.string.about_privacy_policy, ABOUT_PRIVACY_POLICY_ROW_TAG, isSubItem = true)

private val SUB_LINK_ENTRIES = listOf(
    AboutLinkEntry(R.drawable.ic_bullet, R.string.about_license_support_lib, testTag = ABOUT_LICENSE_SUPPORT_TAG, isSubItem = true),
    AboutLinkEntry(R.drawable.ic_bullet, R.string.about_license_dagger, testTag = ABOUT_LICENSE_DAGGER_TAG, isSubItem = true),
    AboutLinkEntry(R.drawable.ic_bullet, R.string.about_license_kotlin, testTag=ABOUT_LICENSE_KOTLIN_TAG, isSubItem = true),
    AboutLinkEntry(R.drawable.ic_bullet, R.string.about_license_material, testTag=ABOUT_LICENSE_MATERIAL_TAG, isSubItem = true),
    AboutLinkEntry(R.drawable.ic_bullet, R.string.about_porter_stemming_algorithm, testTag=ABOUT_LICENSE_PORTER_STEMMER_TAG, isSubItem = true),
)

/**
 * The about screen: app version, copyright, links, and license entries.
 *
 * URLs and email addresses in the text are clickable links, like the
 * old `autoLink="all"` TextViews.
 *
 * @param appVersionText The formatted "app version" line.
 * @param onNavigateUp Called when the up affordance is tapped.
 * @param onOpenLicense Called with the license title and asset file when
 * a license row is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    appVersionText: String,
    onNavigateUp: () -> Unit,
    onOpenLicense: (title: String, licenseAssetFile: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.about_title, stringResource(R.string.app_name)))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.abc_action_bar_up_description),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = stringResource(R.string.app_name),
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomEnd,
                alpha = 0.2f,
                modifier = Modifier.fillMaxSize(),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Text(
                    text = appVersionText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                LinkText(
                    text = stringResource(R.string.about_copyright),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp),
                )
                TOP_LINK_ENTRIES.forEach { entry ->
                    AboutRow(
                        iconRes = entry.iconRes,
                        text = stringResource(entry.textRes),
                        testTag = entry.testTag,
                        isSubItem = false,
                    )
                }
                AboutRow(
                    iconRes = PRIVACY_POLICY_ENTRY.iconRes,
                    text = stringResource(PRIVACY_POLICY_ENTRY.textRes),
                    testTag = PRIVACY_POLICY_ENTRY.testTag,
                    isSubItem = true,
                )
                LICENSE_ENTRIES.forEach { entry ->
                    val label = stringResource(entry.labelRes)
                    val title = stringResource(entry.titleRes)
                    AboutRow(
                        iconRes = R.drawable.ic_bullet,
                        text = label,
                        testTag = entry.testTag,
                        isSubItem = true,
                        isLink = true,
                        onClick = { onOpenLicense(title, entry.assetFile) },
                    )
                }
                SUB_LINK_ENTRIES.forEach { entry ->
                    AboutRow(
                        iconRes = entry.iconRes,
                        text = stringResource(entry.textRes),
                        testTag = entry.testTag,
                        isSubItem = true,
                    )
                }
            }
        }
    }
}

/**
 * One row of the about screen: an icon followed by text. When [onClick]
 * is set, the whole row is clickable and the text is shown as a link.
 */
@Composable
private fun AboutRow(
    @DrawableRes iconRes: Int,
    text: String,
    testTag: String,
    isSubItem: Boolean = false,
    isLink: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .then(if (isSubItem) Modifier.padding(start = 24.dp, top = 8.dp) else Modifier.padding(top = 16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).testTag(testTag),

    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
        )
        LinkText(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isLink) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)
        )
    }
}

/**
 * Text with URLs and email addresses turned into clickable links, like
 * the old `autoLink="all"` TextViews. Link clicks are handled by [Text]
 * through the local uri handler.
 */
@Composable
private fun LinkText(
    text: String,
    style: TextStyle,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val linkSpanStyle = SpanStyle(
        color = linkColor,
        textDecoration = TextDecoration.Underline,
    )
    val annotatedString = buildAnnotatedString {
        append(text)
        URL_REGEX.findAll(text).forEach { match ->
            addLink(
                LinkAnnotation.Url(match.value, TextLinkStyles(style = linkSpanStyle)),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }
        EMAIL_REGEX.findAll(text).forEach { match ->
            addLink(
                LinkAnnotation.Url("mailto:${match.value}", TextLinkStyles(style = linkSpanStyle)),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }
    }
    Text(
        text = annotatedString,
        style = style,
        color = color,
        modifier = modifier,
    )
}

@Composable
@ExcludeFromJacocoGeneratedReport
@Preview(showBackground = true)
fun AboutScreenPreview() {
    AppTheme {
        AboutScreen(
            appVersionText = "Poet Assistant v1.0",
            onNavigateUp = {},
            onOpenLicense = { _, _ -> },
        )
    }
}
