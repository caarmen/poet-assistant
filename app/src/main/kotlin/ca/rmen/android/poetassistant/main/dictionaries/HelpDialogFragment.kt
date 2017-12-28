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

package ca.rmen.android.poetassistant.main.dictionaries

import android.app.Dialog
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.compat.HtmlCompat

/**
 * Shows a dialog with a title, message, and ok button.
 */
class HelpDialogFragment : DialogFragment() {
    companion object {
        private val TAG = Constants.TAG + HelpDialogFragment::class.java.simpleName
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_MESSAGE = "extra_message"
        fun create(@StringRes titleId: Int, @StringRes messageId: Int): HelpDialogFragment {
            val fragment = HelpDialogFragment()
            val arguments = Bundle(2)
            arguments.putInt(EXTRA_TITLE, titleId)
            arguments.putInt(EXTRA_MESSAGE, messageId)
            fragment.arguments = arguments
            return fragment
        }
    }

    /**
     * @return a Dialog with a title, message and ok button.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.v(TAG, "onCreateDialog: savedInstanceState = $savedInstanceState")
        context?.let { context ->
            arguments?.let { arguments ->
                /*@StringRes*/
                val titleId = arguments.getInt(EXTRA_TITLE)
                /*@StringRes*/
                val messageId = arguments.getInt(EXTRA_MESSAGE)
                return AlertDialog.Builder(context)
                        .setTitle(context.getString(titleId))
                        .setMessage(HtmlCompat.fromHtml(context.getString(messageId)))
                        .setPositiveButton(android.R.string.ok, null)
                        .create()
            }
        }
        return super.onCreateDialog(savedInstanceState)
    }
}
