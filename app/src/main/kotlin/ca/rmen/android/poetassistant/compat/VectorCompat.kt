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

import android.app.Activity
import android.os.Build
import android.text.style.ImageSpan
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

object VectorCompat {
    fun createVectorImageSpan(activity: Activity, @DrawableRes resId: Int): ImageSpan {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ImageSpan(activity, resId)
        }

        val drawable = createVectorDrawable(activity, resId)
        drawable!!.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        return ImageSpan(drawable)
    }

    /**
     * AppCompat 23.3.0 removed support of vector drawables attributes like
     * drawableLeft, from xml.  Use this method to use a vector drawable as
     * a compound drawable of a TextView.
     */
    fun setCompoundVectorDrawables(activity: Activity,
                                   textView: TextView,
                                   @DrawableRes start: Int,
                                   @DrawableRes top: Int,
                                   @DrawableRes end: Int,
                                   @DrawableRes bottom: Int) {
        androidx.core.widget.TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                textView,
                createVectorDrawable(activity, start),
                createVectorDrawable(activity, top),
                createVectorDrawable(activity, end),
                createVectorDrawable(activity, bottom))
    }

    private fun createVectorDrawable(activity: Activity, @DrawableRes res: Int): VectorDrawableCompat? {
        if (res == 0) return null
        return VectorDrawableCompat.create(activity.resources, res, null)
    }
}
