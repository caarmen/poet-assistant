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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.widget.TextView;

public final class VectorCompat {
    private VectorCompat() {
        // prevent instantiation
    }

    /**
     * AppCompat 23.3.0 removed support of vector drawables attributes like
     * drawableLeft, from xml.  Use this method to use a vector drawable as
     * a compound drawable of a TextView.
     */
    public static void setCompoundVectorDrawables(Context context,
                                                  TextView textView,
                                                  @DrawableRes int start,
                                                  @DrawableRes int top,
                                                  @DrawableRes int end,
                                                  @DrawableRes int bottom) {
        // On Marshmallow, if we use the setCompoundDrawables* methods which take
        // VectorDrawableCompat arguments, the rendering is incorrect (the text
        // appears on top of the icon).  If we use the setCompoundDrawable* methods
        // which take resource ids instead, we don't have this problem. Since the
        // method which takes resource ids is available on lollipop, we use
        // it starting from lollipop.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setCompoundVectorDrawablesV21(textView, start, top, end, bottom);
        } else {
            android.support.v4.widget.TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    textView,
                    createVectorDrawable(context, start),
                    createVectorDrawable(context, top),
                    createVectorDrawable(context, end),
                    createVectorDrawable(context, bottom));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setCompoundVectorDrawablesV21(TextView textView,
                                                      @DrawableRes int start,
                                                      @DrawableRes int top,
                                                      @DrawableRes int end,
                                                      @DrawableRes int bottom) {
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
    }

    private static VectorDrawableCompat createVectorDrawable(Context context, @DrawableRes int res) {
        if (res == 0) return null;
        return VectorDrawableCompat.create(context.getResources(), res, null);
    }
}
