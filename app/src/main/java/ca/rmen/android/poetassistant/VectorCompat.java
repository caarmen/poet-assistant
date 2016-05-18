/*
 * Copyright (c) 2016 Carmen Alvarez
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

package ca.rmen.android.poetassistant;

import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.text.style.ImageSpan;
import android.widget.TextView;

public final class VectorCompat {
    private VectorCompat() {
        // prevent instantiation
    }

    @SuppressWarnings("SameParameterValue")
    public static ImageSpan createVectorImageSpan(Context context, @DrawableRes int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new ImageSpan(context, resId);
        }
        VectorDrawableCompat drawable = createVectorDrawable(context, resId);
        assert drawable != null;
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        return new ImageSpan(drawable);
    }

    /**
     * AppCompat 23.3.0 removed support of vector drawables attributes like
     * drawableLeft, from xml.  Use this method to use a vector drawable as
     * a compound drawable of a TextView.
     */
    @SuppressWarnings("SameParameterValue")
    public static void setCompoundVectorDrawables(Context context,
                                                  TextView textView,
                                                  @DrawableRes int start,
                                                  @DrawableRes int top,
                                                  @DrawableRes int end,
                                                  @DrawableRes int bottom) {
        android.support.v4.widget.TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                textView,
                createVectorDrawable(context, start),
                createVectorDrawable(context, top),
                createVectorDrawable(context, end),
                createVectorDrawable(context, bottom));
    }

    private static VectorDrawableCompat createVectorDrawable(Context context, @DrawableRes int res) {
        if (res == 0) return null;
        return VectorDrawableCompat.create(context.getResources(), res, null);
    }
}
