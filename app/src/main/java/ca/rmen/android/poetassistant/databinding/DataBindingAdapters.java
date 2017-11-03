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

package ca.rmen.android.poetassistant.databinding;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressWarnings("WeakerAccess")
final class DataBindingAdapters {
    @BindingAdapter("srcCompat")
    public static void setImageResource(ImageView imageView, @DrawableRes int resource) {
        imageView.setImageResource(resource);
    }

    @BindingAdapter("enabled")
    public static void setEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
    }

    @BindingAdapter("charSequence")
    public static void setCharSequence(TextView textView, CharSequence charSequence) {
        if (!TextUtils.equals(textView.getText(), charSequence) || !textView.getText().getClass().equals(charSequence.getClass())) {
            textView.setText(charSequence);
        }
    }

    @BindingAdapter("charSequenceChanged")
    public static void setCharSequenceChanged(EditText textView, InverseBindingListener listener) {
        textView.setOnClickListener(view -> listener.onChange());
    }

    @InverseBindingAdapter(attribute = "charSequence", event = "charSequenceChanged")
    public static CharSequence getCharSequence(TextView textView) {
        return textView.getText();
    }

}
