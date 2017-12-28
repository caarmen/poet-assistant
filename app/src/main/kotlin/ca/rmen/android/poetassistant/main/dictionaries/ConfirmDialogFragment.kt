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
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.util.Log
import ca.rmen.android.poetassistant.Constants

/**
 * Shows a dialog with a title, message, and ok/cancel button.
 * The activity or fragment which adds this dialog must implement the
 * ConfirmDialogListener interface.
 */
class ConfirmDialogFragment : DialogFragment() {
    companion object {
        private val TAG = Constants.TAG + ConfirmDialogFragment::class.java.simpleName
        private const val EXTRA_ACTION_ID = "action_id"
        private const val EXTRA_MESSAGE = "message"
        private const val EXTRA_POSITIVE_ACTION = "positive_action"
        fun show(actionId: Int, message: String, positiveAction: String, fragmentManager: FragmentManager, tag: String) {
            val fragment = ConfirmDialogFragment()
            val bundle = Bundle(3)
            bundle.putInt(EXTRA_ACTION_ID, actionId)
            bundle.putString(EXTRA_MESSAGE, message)
            bundle.putString(EXTRA_POSITIVE_ACTION, positiveAction)
            fragment.arguments = bundle
            fragmentManager.beginTransaction().add(fragment, tag).commit()
        }
    }

    interface ConfirmDialogListener {
        fun onOk(actionId: Int)
    }

    /**
     * @return a Dialog with a title, message, and ok/cancel buttons.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.v(TAG, "onCreateDialog: savedInstanceState = $savedInstanceState")
        activity?.let { context ->
            arguments?.let { args ->
                val actionId = args.getInt(EXTRA_ACTION_ID)
                val positiveListener = DialogInterface.OnClickListener { _, _ ->
                    val listener = if (parentFragment is ConfirmDialogListener) parentFragment as ConfirmDialogListener
                    else context as ConfirmDialogListener
                    listener.onOk(actionId)
                }
                return AlertDialog.Builder(context)
                        .setMessage(args.getString(EXTRA_MESSAGE))
                        .setPositiveButton(args.getString(EXTRA_POSITIVE_ACTION), positiveListener)
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
            }

        }
        return super.onCreateDialog(savedInstanceState)
    }
}
