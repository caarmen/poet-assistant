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

package ca.rmen.android.poetassistant.main

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R

/**
 * Shows a dialog with a title, message, and ok button.
 * The activity or fragment which adds this dialog should implement the
 * WarningDialogListener interface.
 */
class WarningNoSpaceDialogFragment : DialogFragment() {
    companion object {
        private val TAG = Constants.TAG + WarningNoSpaceDialogFragment::class.java.simpleName
    }

    interface WarningNoSpaceDialogListener {
        fun onWarningNoSpaceDialogDismissed()
    }

    /**
     * @return a Dialog with a title, message and ok button.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.v(TAG, "onCreateDialog: savedInstanceState: $savedInstanceState")
        context?.let {
            val positiveListener = DialogInterface.OnClickListener { _, _ -> notifyListener() }
            val dismissListener = DialogInterface.OnDismissListener { _ -> notifyListener() }

            val dialog = AlertDialog.Builder(it)
                    .setTitle(it.getString(R.string.warning_no_space_title))
                    .setMessage(it.getString(R.string.warning_no_space_message))
                    .setPositiveButton(android.R.string.ok, positiveListener)
                    .setOnDismissListener(dismissListener)
                    .create()
            dialog.setOnDismissListener(dismissListener)
            return dialog
        }
        return super.onCreateDialog(savedInstanceState)
    }

    private fun notifyListener() {
        if (parentFragment is WarningNoSpaceDialogListener) {
            (parentFragment as WarningNoSpaceDialogListener).onWarningNoSpaceDialogDismissed()
        } else if (activity is WarningNoSpaceDialogListener) {
            (activity as WarningNoSpaceDialogListener).onWarningNoSpaceDialogDismissed()
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        notifyListener()
    }
}