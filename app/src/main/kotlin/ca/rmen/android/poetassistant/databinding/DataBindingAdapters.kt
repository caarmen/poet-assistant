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

package ca.rmen.android.poetassistant.databinding

import androidx.databinding.BindingAdapter
import androidx.annotation.DrawableRes
import android.widget.ImageView
import com.google.android.material.button.MaterialButton

object DataBindingAdapters {
    @JvmStatic
    @BindingAdapter("srcCompat")
    fun setImageResource(imageView: ImageView, @DrawableRes resource: Int) {
        imageView.setImageResource(resource)
    }

    @JvmStatic
    @BindingAdapter("iconCompat")
    fun setIcon(button: MaterialButton, @DrawableRes resource: Int) {
        button.setIconResource(resource)
    }
}
