/*
 * Copyright (c) 2016 - 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.compat

import android.content.Context
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.text.style.ImageSpan
import android.widget.TextView

object VectorCompat {
    fun createVectorImageSpan(context: Context, @DrawableRes resId: Int): ImageSpan {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ImageSpan(context, resId)
        }

        val drawable = createVectorDrawable(context, resId)
        drawable!!.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        return ImageSpan(drawable)
    }

    /**
     * AppCompat 23.3.0 removed support of vector drawables attributes like
     * drawableLeft, from xml.  Use this method to use a vector drawable as
     * a compound drawable of a TextView.
     */
    fun setCompoundVectorDrawables(context: Context,
                                   textView: TextView,
                                   @DrawableRes start: Int,
                                   @DrawableRes top: Int,
                                   @DrawableRes end: Int,
                                   @DrawableRes bottom: Int) {
        android.support.v4.widget.TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                textView,
                createVectorDrawable(context, start),
                createVectorDrawable(context, top),
                createVectorDrawable(context, end),
                createVectorDrawable(context, bottom))
    }

    private fun createVectorDrawable(context: Context, @DrawableRes res: Int): VectorDrawableCompat? {
        if (res == 0) return null
        return VectorDrawableCompat.create(context.resources, res, null)
    }
}
