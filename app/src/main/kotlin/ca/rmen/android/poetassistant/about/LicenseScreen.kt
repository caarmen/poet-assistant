/*
 * Copyright (c) 2025 - present Carmen Alvarez
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

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.rmen.android.poetassistant.R

@Composable
fun LicenseScreen(
    title: String,
    licenseText: String,
    modifier: Modifier = Modifier,
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(16.dp)
            .horizontalScroll(enabled = true, state = horizontalScrollState)
            .verticalScroll(enabled = true, state = verticalScrollState)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Text(
            text = licenseText,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
@Preview(showBackground = true, heightDp = 300)
fun LicenseScreenPreview() {
    LicenseScreen(
        title = stringResource(R.string.about_license_thesaurus), licenseText = """
             The app uses the WordNet thesaurus.

             The WordNet thesaurus is used in LibreOffice and OpenOffice,
             in conjunction with other English-language dictionaries and thesauruses.

             The version of the WordNet thesaurus used in this thesaurus library is
             found in the LibreOffice repository: https://github.com/LibreOffice/dictionaries/tree/master/en
             The thesaurus file is th_en_US_v2.dat.

             Multiple license files exist in this folder of the LibreOffice repository:
             * README.txt contains a BSD license for the Hunspell en_US dictionary
             * license.txt contains a GPL v2 license
             * WordNet_license.txt contains a specific license from Princeton University.

             To the best of my knowledge, the WordNet_license.txt file is the relevant license
             for the thesaurus data used in the thesaurus library. The contents of that license are:

             ======================================================================
             WordNet Release 2.1

             This software and database is being provided to you, the LICENSEE, by
             Princeton University under the following license.  By obtaining, using
             and/or copying this software and database, you agree that you have
             read, understood, and will comply with these terms and conditions.:  

        """.trimIndent()
    )

}