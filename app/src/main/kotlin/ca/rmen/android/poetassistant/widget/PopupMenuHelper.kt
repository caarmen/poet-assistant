/*
 * Copyright (c) 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.widget

import android.content.Context
import androidx.appcompat.widget.PopupMenu
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.Menu
import android.view.MenuItem
import ca.rmen.android.poetassistant.R

object PopupMenuHelper {
    private const val POPUP_MENU_ICON_PLACEHOLDER = "       "

    /**
     * Moves icons from the PopupMenu's MenuItems' icon fields into the menu title as a Spannable with the title text.
     */
    fun insertMenuItemIcons(context : Context, popupMenu : PopupMenu) {
        val menu = popupMenu.menu
        if (hasIcon(menu)) {
            for (i in 0 until menu.size()) {
                insertMenuItemIcon(context, menu.getItem(i))
            }
        }
    }

    /**
     * @return true if the menu has at least one MenuItem with an icon.
     */
    private fun hasIcon(menu : Menu) : Boolean {
        return (0 until menu.size()).any { menu.getItem(it).icon != null }
    }

    /**
     * Converts the given MenuItem's title into a Spannable containing both its icon and title.
     */
    private fun insertMenuItemIcon(context : Context, menuItem : MenuItem) {
        if (menuItem.icon == null) return

        menuItem.icon.setBounds(0, 0, menuItem.icon.intrinsicWidth, menuItem.icon.intrinsicHeight)
        val iconSize = context.resources.getDimensionPixelSize(R.dimen.menu_item_icon_size)
        menuItem.icon.setBounds(0, 0, iconSize, iconSize)
        val imageSpan = ImageSpan(menuItem.icon)

        // Add a space placeholder for the icon, before the title.
        val ssb = SpannableStringBuilder(POPUP_MENU_ICON_PLACEHOLDER + menuItem.title)

        // Replace the space placeholder with the icon.
        ssb.setSpan(imageSpan, 1, 2, 0)
        menuItem.title = ssb

        // Set the icon to null just in case, on some weird devices, they've customized Android to display
        // the icon in the menu... we don't want two icons to appear.
        menuItem.icon = null
    }
}
