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
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.databinding.InputDialogEditTextBinding
import ca.rmen.android.poetassistant.main.AppBarLayoutHelper

/**
 * Shows a dialog prompting the user enter a word to filter search results.
 * The activity or fragment which adds this dialog must implement the
 * FilterDialogListener interface.
 */
class FilterDialogFragment : DialogFragment() {
    companion object {
        private val TAG = Constants.TAG + FilterDialogFragment::class.java.simpleName
        private const val EXTRA_MESSAGE = "message"
        private const val EXTRA_TEXT = "text"

        fun newInstance(message: String, text: String?): FilterDialogFragment {
            val fragment = FilterDialogFragment()
            val bundle = Bundle(2)
            bundle.putString(EXTRA_MESSAGE, message)
            bundle.putString(EXTRA_TEXT, text)
            fragment.arguments = bundle
            return fragment
        }
    }

    interface FilterDialogListener {
        fun onFilterSubmitted(input: String)
    }

    /**
     * @return a Dialog with a title, message, an edit text, and ok/cancel buttons.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.v(TAG, "onCreateDialog: savedInstanceState=$savedInstanceState")
        activity?.let { context ->
            val themedLayoutInflater = LayoutInflater.from(ContextThemeWrapper(context, R.style.AppAlertDialog))
            val binding = DataBindingUtil.inflate<InputDialogEditTextBinding>(themedLayoutInflater,
                    R.layout.input_dialog_edit_text, null, false)
            arguments?.let { args ->
                binding.edit.setText(args.getString(EXTRA_TEXT))

                val positiveListener = DialogInterface.OnClickListener { _, _->
                    val listener = if (parentFragment is FilterDialogListener) parentFragment as FilterDialogListener else activity as FilterDialogListener
                    listener.onFilterSubmitted(binding.edit.text.toString())
                }

                val dialog = AlertDialog.Builder(context)
                        .setView(binding.root)
                        .setTitle(R.string.filter_title)
                        .setMessage(args.getString(EXTRA_MESSAGE))
                        .setPositiveButton(android.R.string.ok, positiveListener)
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                binding.edit.setOnFocusChangeListener { _, hasFocus ->
                    dialog.window?.let { window ->
                        if (hasFocus) {
                            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                        } else {
                            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                        }
                    }
                }
                return dialog
            }
        }
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        AppBarLayoutHelper.forceExpandAppBarLayout(activity)
    }
}
