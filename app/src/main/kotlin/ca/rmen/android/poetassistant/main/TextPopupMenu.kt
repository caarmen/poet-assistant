/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.di.NonAndroidEntryPoint
import ca.rmen.android.poetassistant.main.dictionaries.Share
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener
import ca.rmen.android.poetassistant.widget.HackFor23381
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.EntryPointAccessors

object TextPopupMenu {

    /**
     * Create a popup menu allowing the user to lookup the selected text in the given text view, in
     * the rhymer, thesaurus, or dictionary.
     *
     * @param textView if the text view has selected text, tapping on it will bring up the popup menu
     * @param listener this listener will be notified when the user selects one of the popup menu items
     */
    fun addSelectionPopupMenu(snackbarView: View, textView: TextView, listener: OnWordClickListener) {
        val settingsPrefs = EntryPointAccessors.fromApplication(snackbarView.context, NonAndroidEntryPoint::class.java).prefs()
        if (!settingsPrefs.isSelectionLookupEnabled) {
            return
        }
        textView.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
                // Issue #78: mode can be null
                if (mode == null) return false
                mode.menuInflater.inflate(R.menu.menu_word_lookup, menu)
                mode.title = null
                return true

            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu): Boolean {
                // https://code.google.com/p/android/issues/detail?id=23381
                if (textView is HackFor23381) (textView as HackFor23381).setWindowFocusWait(true)
                for (i in 0 until menu.size()) {
                    val menuItem = menu.getItem(i)
                    val intent = menuItem.intent
                    // Hide our own process text action meant for other apps.
                    if (intent != null
                            && Intent.ACTION_PROCESS_TEXT == intent.action
                            && intent.component != null
                            && textView.context.applicationInfo.packageName == intent.component?.packageName) {
                        menuItem.isVisible = false
                    }
                }
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
                return handleItemClicked(item.itemId, snackbarView, textView, getSelectedWord(textView), listener)
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                // https://code.google.com/p/android/issues/detail?id=23381
                if (textView is HackFor23381) (textView as HackFor23381).setWindowFocusWait(false)
            }
        }
    }

    private fun handleItemClicked(itemId: Int, snackbarView: View, view: View, selectedWord: String?, listener: OnWordClickListener): Boolean {
        if (TextUtils.isEmpty(selectedWord)) {
            return false
        }
        when (itemId) {
            R.id.action_lookup_rhymer -> listener.onWordClick(selectedWord!!, Tab.RHYMER)
            R.id.action_lookup_thesaurus -> listener.onWordClick(selectedWord!!, Tab.THESAURUS)
            R.id.action_lookup_dictionary -> listener.onWordClick(selectedWord!!, Tab.DICTIONARY)
            R.id.action_copy -> {
                val clipboard = view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                if (clipboard != null) {
                    val clip = ClipData.newPlainText(selectedWord, selectedWord)
                    clipboard.setPrimaryClip(clip)
                    Snackbar.make(snackbarView, R.string.snackbar_copied_text, Snackbar.LENGTH_SHORT).show()
                }
            }
            R.id.action_share -> Share.share(view.context, selectedWord!!)
            else -> return false
        }
        return true
    }

    private fun getSelectedWord(textView: TextView): String? {
        val selectionStart = textView.selectionStart
        val selectionEnd = textView.selectionEnd

        // The user selected some text, use that (even if it contains partial words)
        if (selectionStart < selectionEnd) {
            val text = textView.text.toString()
            return text.substring(selectionStart, selectionEnd)
        }
        return null
    }
}
