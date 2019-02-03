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

import android.annotation.TargetApi
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Build
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.widget.PopupMenu
import android.text.TextUtils
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.dagger.DaggerHelper
import ca.rmen.android.poetassistant.main.dictionaries.Share
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener
import ca.rmen.android.poetassistant.widget.HackFor23381
import ca.rmen.android.poetassistant.widget.PopupMenuHelper

object TextPopupMenu {
    enum class Style {
        /**
         * Denotes popup menu items for copying text, and any other actions provided by the system.
         */
        SYSTEM,
        /**
         * Denotes popup menu items for looking up a text in the rhymer, thesaurus, or dictionary, as well as SYSTEM items.
         */
        FULL
    }

    /**
     * Create a popup menu allowing the user perform actions on the whole text of the given text view.
     *
     * @param style    determines what popup menu items will appear. See {@link Style}.
     * @param textView tapping on it will bring up the popup menu
     * @param listener this listener will be notified when the user selects one of the popup menu items
     */
    fun addPopupMenu(style: Style, snackbarView: View, textView: TextView, listener: OnWordClickListener) {
        textView.setOnClickListener {
            val text = textView.text.toString()
            val popupMenu = createPopupMenu(snackbarView, textView, text, listener)
            when (style) {
                Style.SYSTEM -> {
                    addSystemMenuItems(textView.context, popupMenu.menuInflater, popupMenu.menu, text)
                    PopupMenuHelper.insertMenuItemIcons(textView.context, popupMenu)
                }
                Style.FULL -> {
                    addAppMenuItems(popupMenu)
                    val systemMenu = popupMenu.menu.addSubMenu(R.string.menu_more)
                    addSystemMenuItems(textView.context, popupMenu.menuInflater, systemMenu, text)
                }
            }
            popupMenu.show()
        }
    }

    /**
     * Create a popup menu allowing the user to lookup the selected text in the given text view, in
     * the rhymer, thesaurus, or dictionary.
     *
     * @param textView if the text view has selected text, tapping on it will bring up the popup menu
     * @param listener this listener will be notified when the user selects one of the popup menu items
     */
    fun addSelectionPopupMenu(snackbarView: View, textView: TextView, listener: OnWordClickListener) {
        val settingsPrefs = DaggerHelper.getMainScreenComponent(textView.context).getSettingsPrefs()
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
                    clipboard.primaryClip = clip
                    Snackbar.make(snackbarView, R.string.snackbar_copied_text, Snackbar.LENGTH_SHORT).show()
                }
            }
            R.id.action_share -> Share.share(view.context, selectedWord!!)
            else -> return false
        }
        return true
    }

    private fun createPopupMenu(snackbarView: View, view: View, selectedWord: String, listener: OnWordClickListener): PopupMenu {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.setOnMenuItemClickListener { item ->
            handleItemClicked(item.itemId, snackbarView, view, selectedWord, listener)
            false
        }
        return popupMenu
    }

    private fun addAppMenuItems(popupMenu: PopupMenu) {
        popupMenu.inflate(R.menu.menu_word_lookup)
    }

    private fun addSystemMenuItems(context: Context, menuInflater: MenuInflater, menu: Menu, text: String) {
        menuInflater.inflate(R.menu.menu_word_other, menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSupportedActivities(context, text)
                    .filter { resolveInfo -> context.applicationInfo.packageName != resolveInfo.activityInfo.packageName }
                    .forEach { resolveInfo ->
                        menu.add(
                                R.id.group_system_popup_menu_items, Menu.NONE,
                                Menu.NONE,
                                resolveInfo.loadLabel(context.packageManager))
                                .setIcon(resolveInfo.loadIcon(context.packageManager))
                                .setIntent(createProcessTextIntentForResolveInfo(resolveInfo, text))
                                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                    }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun getSupportedActivities(context: Context, text: String): List<ResolveInfo> {
        //https://android-developers.googleblog.com/2015/10/in-app-translations-in-android.html?hl=mk
        return context.packageManager.queryIntentActivities(createProcessTextIntent(text), 0)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun createProcessTextIntent(text: String): Intent {
        //https://android-developers.googleblog.com/2015/10/in-app-translations-in-android.html?hl=mk
        return Intent()
                .setAction(Intent.ACTION_PROCESS_TEXT)
                .putExtra(Intent.EXTRA_PROCESS_TEXT, text)
                .setType("text/plain")
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun createProcessTextIntentForResolveInfo(info: ResolveInfo, text: String): Intent {
        //https://android-developers.googleblog.com/2015/10/in-app-translations-in-android.html?hl=mk
        return createProcessTextIntent(text)
                .putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
                .setClassName(info.activityInfo.packageName,
                        info.activityInfo.name)
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
