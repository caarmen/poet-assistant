/*
 * Copyright (c) 2016-2017 Carmen Alvarez
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

import androidx.lifecycle.Observer
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.TtsState
import ca.rmen.android.poetassistant.databinding.ResultListHeaderBinding
import ca.rmen.android.poetassistant.main.Tab
import java.util.Locale

class ResultListHeaderFragment : Fragment(), FilterDialogFragment.FilterDialogListener, ConfirmDialogFragment.ConfirmDialogListener {
    companion object {
        private val TAG = Constants.TAG + ResultListHeaderFragment::class.java.simpleName
        private const val ACTION_CLEAR_FAVORITES = 1
        private const val DIALOG_TAG = "dialog"
        private const val EXTRA_TAB = "tab"

        fun newInstance(tab: Tab): ResultListHeaderFragment {
            val arguments = Bundle(1)
            arguments.putSerializable(EXTRA_TAB, tab)
            val fragment = ResultListHeaderFragment()
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var mTab: Tab
    private lateinit var mBinding: ResultListHeaderBinding
    private lateinit var mViewModel: ResultListHeaderViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "onCreateView, savedInstanceState = $savedInstanceState")
        if (arguments == null) {
            return super.onCreateView(inflater, container, savedInstanceState)
        }
        arguments?.let {
            mTab = it.getSerializable(EXTRA_TAB) as Tab
        }

        mBinding = DataBindingUtil.inflate(inflater, R.layout.result_list_header, container, false)
        context?.let {
            mBinding.tvFilterLabel.text = ResultListFactory.getFilterLabel(it, mTab)
        }
        mBinding.buttonListener = ButtonListener()
        parentFragment?.let {
            mViewModel = ViewModelProvider(it).get(ResultListHeaderViewModel::class.java)
            mBinding.viewModel = mViewModel
            mViewModel.snackbarText.observe(this, mSnackbarTextChanged)
            mViewModel.isFavoriteLiveData.observe(this, mFavoriteObserver)
            mViewModel.ttsStateLiveData.observe(this, mTtsObserver)
        }
        return mBinding.root

    }

    override fun onFilterSubmitted(input: String) {
        mViewModel.filter.set(input.lowercase(Locale.getDefault()).trim())
    }

    override fun onOk(actionId: Int) {
        if (actionId == ACTION_CLEAR_FAVORITES) {
            mViewModel.clearFavorites()
        }
    }

    private val mSnackbarTextChanged = Observer<String> { text ->
        if (!TextUtils.isEmpty(text)) {
            Snackbar.make(mBinding.root, text!!, Snackbar.LENGTH_SHORT).show()
        }
    }

    private val mFavoriteObserver = Observer<Boolean> { isFavorite -> mBinding.btnStarQuery.isChecked = isFavorite == true }

    private val mTtsObserver = Observer<TtsState> { ttsState ->
        Log.d(TAG, "$mTab: ttsState = $ttsState")
        if (ttsState != null) ResultListFactory.updateListHeaderButtonsVisibility(mBinding, mTab, ttsState.currentStatus)
    }

    inner class ButtonListener {
        fun onDeleteFavoritesButtonClicked(@Suppress("UNUSED_PARAMETER") v: View) {
            ConfirmDialogFragment.show(
                    ACTION_CLEAR_FAVORITES,
                    getString(R.string.action_clear_favorites),
                    getString(R.string.action_clear),
                    childFragmentManager,
                    DIALOG_TAG)
        }

        fun onFilterButtonClicked(@Suppress("UNUSED_PARAMETER") v: View) {
            context?.let {
                val fragment = ResultListFactory.createFilterDialog(it, mTab, mViewModel.filter.get())
                childFragmentManager.beginTransaction().add(fragment, DIALOG_TAG).commit()
            }
        }

        fun onHelpButtonClicked(@Suppress("UNUSED_PARAMETER") v: View) {
            childFragmentManager.beginTransaction()
                    .add(HelpDialogFragment.create(R.string.pattern_help_title, R.string.pattern_help_message), DIALOG_TAG)
                    .commit()
        }
    }

}
