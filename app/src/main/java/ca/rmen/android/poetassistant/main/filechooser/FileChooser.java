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

package ca.rmen.android.poetassistant.main.filechooser;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

import ca.rmen.android.poetassistant.R;

class FileChooser {
    private FileChooser() {
        // utility class
    }

    static String getShortDisplayName(Context context, File file) {
        if (file.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath()))
            return context.getString(R.string.file_chooser_sdcard);
        else if (TextUtils.isEmpty(file.getName()))
            return context.getString(R.string.file_chooser_root);
        else
            return file.getName();
    }

    static String getFullDisplayName(Context context, File file) {
        if (file.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath()))
            return context.getString(R.string.file_chooser_sdcard);
        else if (TextUtils.isEmpty(file.getName()))
            return context.getString(R.string.file_chooser_root);
        else
            return file.getAbsolutePath();
    }

}
