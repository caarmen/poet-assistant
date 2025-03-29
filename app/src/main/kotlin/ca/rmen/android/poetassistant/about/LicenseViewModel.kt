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

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.di.IODispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LicenseViewModel @Inject constructor(
    private val application: Application,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) :
    AndroidViewModel(application) {

    companion object {
        private val TAG = Constants.TAG + LicenseViewModel::class.java.simpleName
    }


    private val _licenseText = MutableStateFlow("")
    val licenseText = _licenseText.asStateFlow()

    fun readLicenseText(fileName: String) {
        viewModelScope.launch(ioDispatcher) {
            val contents = readFile(fileName)
            _licenseText.value = contents
        }
    }

    private fun readFile(fileName: String): String {
        try {
            return application.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e(TAG, "Couldn't read license file $fileName: ${e.message}", e)
            return ""
        }
    }
}