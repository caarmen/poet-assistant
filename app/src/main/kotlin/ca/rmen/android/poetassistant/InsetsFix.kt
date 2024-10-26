/*
 * Copyright (c) 2024 Carmen Alvarez
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

package ca.rmen.android.poetassistant

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

fun fixInsets(view: View) {
    // Issue #206:
    // https://developer.android.com/develop/ui/views/layout/edge-to-edge#system-bars-insets
    ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars()
                    or WindowInsetsCompat.Type.displayCutout()
        )
        v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = insets.top
            leftMargin = insets.left
            bottomMargin = insets.bottom
            rightMargin = insets.right
        }
        WindowInsetsCompat.CONSUMED
    }
}
