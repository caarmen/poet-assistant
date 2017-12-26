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

package ca.rmen.android.poetassistant.main.reader

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.IdRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import ca.rmen.android.poetassistant.Constants
import ca.rmen.android.poetassistant.R
import ca.rmen.android.poetassistant.compat.HtmlCompat
import ca.rmen.android.poetassistant.databinding.FragmentReaderBinding
import ca.rmen.android.poetassistant.main.AppBarLayoutHelper
import ca.rmen.android.poetassistant.main.TextPopupMenu
import ca.rmen.android.poetassistant.main.dictionaries.ConfirmDialogFragment
import ca.rmen.android.poetassistant.main.dictionaries.HelpDialogFragment
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener
import ca.rmen.android.poetassistant.settings.SettingsActivity
import ca.rmen.android.poetassistant.widget.CABEditText
import ca.rmen.android.poetassistant.widget.DebounceTextWatcher

class ReaderFragment : Fragment(), ConfirmDialogFragment.ConfirmDialogListener {
    companion object {
        private val TAG = Constants.TAG + ReaderFragment::class.java.simpleName
        private const val EXTRA_INITIAL_TEXT = "initial_text"
        private const val DIALOG_TAG = "dialog"
        private const val ACTION_FILE_OPEN = 0
        private const val ACTION_FILE_SAVE_AS = 1
        private const val ACTION_FILE_NEW = 2
        fun newInstance(initialText: String?): ReaderFragment {
            Log.d(TAG, "newInstance: initialText = $initialText")
            val fragment = ReaderFragment()
            fragment.retainInstance = true
            val bundle = Bundle(1)
            bundle.putString(EXTRA_INITIAL_TEXT, initialText)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var mViewModel: ReaderViewModel
    private lateinit var mHandler: Handler
    private lateinit var mBinding: FragmentReaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "onCreate: savedInstanceState = $savedInstanceState")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mHandler = Handler()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated: savedInstanceState = $savedInstanceState")
        loadPoem()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView: inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState")
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_reader, container, false)
        mBinding.buttonListener = ButtonListener()
        mViewModel = ViewModelProviders.of(this).get(ReaderViewModel::class.java)
        mBinding.viewModel = mViewModel
        mViewModel.snackbarText.observe(this, mSnackbarCallback)
        mViewModel.ttsError.observe(this, mTtsErrorCallback)
        mViewModel.poemFile.observe(this, mPoemFileCallback)
        mBinding.tvText.imeListener = object : CABEditText.ImeListener {
            override fun onImeClosed() {
                AppBarLayoutHelper.forceExpandAppBarLayout(activity)
            }
        }
        DebounceTextWatcher.observe(mBinding.tvText).subscribe({ _ -> mViewModel.updatePoemText() })
        TextPopupMenu.addSelectionPopupMenu(mBinding.tvText, activity as OnWordClickListener)
        mViewModel.playButtonStateLiveData.observe(this, mPlayButtonStateObserver)
        return mBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        Log.d(TAG, "onCreateOptionsMenu: menu=$menu, inflater=$inflater")
        inflater.inflate(R.menu.menu_tts, menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) menu.findItem(R.id.action_share).isVisible = false
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        prepareMenuItemsRequiringEnteredText(menu, R.id.action_new, R.id.action_save_as,
                R.id.action_share, R.id.action_share_poem_text, R.id.action_share_poem_audio, R.id.action_print)
        val menuItem = menu.findItem(R.id.action_save)
        if (menuItem == null) {
            Log.d(TAG, "Unexpected: save menu item missing from reader fragment. Monkey?")
        } else {
            menuItem.isEnabled = mViewModel.poemFile.value != null
        }
    }

    private fun prepareMenuItemsRequiringEnteredText(menu: Menu, @IdRes vararg menuIds: Int) {
        val hasEnteredText = !TextUtils.isEmpty(mViewModel.poem.get())
        menuIds.forEach {
            menu.findItem(it)?.isEnabled = hasEnteredText
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_new) {
            ConfirmDialogFragment.show(ACTION_FILE_NEW,
                    getString(R.string.file_new_confirm_title),
                    getString(R.string.action_clear),
                    childFragmentManager,
                    DIALOG_TAG)
        } else if (item.itemId == R.id.action_share_poem_text || item.itemId == R.id.action_share) {
            mViewModel.sharePoem()
        } else if (item.itemId == R.id.action_share_poem_audio) {
            mViewModel.speakToFile()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val context = activity
            if (item.itemId == R.id.action_open) {
                startActivityForResult(mViewModel.getOpenFileIntent(), ACTION_FILE_OPEN)
            } else if (item.itemId == R.id.action_save && context != null) {
                mViewModel.save(context)
            } else if (item.itemId == R.id.action_save_as) {
                startActivityForResult(mViewModel.getSaveAsFileIntent(), ACTION_FILE_SAVE_AS)
            } else if (item.itemId == R.id.action_print && context != null) {
                mViewModel.print(context)
            }
        }
        return true
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        mViewModel.updatePoemText()
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode, data=$data")
        super.onActivityResult(requestCode, resultCode, data)
        val context = activity
        if (context != null) {
            if (requestCode == ACTION_FILE_OPEN && resultCode == Activity.RESULT_OK && data != null) {
                mViewModel.open(context, data.data)
            } else if (requestCode == ACTION_FILE_SAVE_AS && resultCode == Activity.RESULT_OK && data != null) {
                mViewModel.saveAs(context, data.data)
            }
        }
    }

    fun setText(text: String) {
        Log.d(TAG, "setText: text=$text")
        mViewModel.setSavedPoem(PoemFile(null, null, text))
    }

    override fun onOk(actionId: Int) {
        if (actionId == ACTION_FILE_NEW) {
            mViewModel.clearPoem()
            // Hack for https://github.com/caarmen/poet-assistant/issues/72
            // On some devices, clearing the poem text auto-hides the app bar layout.
            // Let's expand it again.
            AppBarLayoutHelper.forceExpandAppBarLayout(activity)
            activity?.invalidateOptionsMenu()
        }
    }

    private fun loadPoem() {
        Log.d(TAG, "loadPoem")
        // First see if we have poem in the arguments
        // (the user chose to share some text with our app)
        arguments?.let {
            val initialText = it.getString(EXTRA_INITIAL_TEXT)
            if (!TextUtils.isEmpty(initialText)) {
                mViewModel.setSavedPoem(PoemFile(null, null, initialText))
                Log.v(TAG, "loadPoem: invalidateOptionsMenu")
                activity?.invalidateOptionsMenu()
                return
            }
        }
        // Load the poem we previously saved
        mViewModel.loadPoem()
    }

    private fun updatePlayButton() {
        val playButtonState = mViewModel.playButtonStateLiveData.value
        Log.v(TAG, "updatePlayButton: playButtonState $playButtonState")
        if (playButtonState != null) {
            mBinding.btnPlay.isEnabled = playButtonState.isEnabled
            mBinding.btnPlay.setImageResource(playButtonState.iconId)
        }
    }

    private val mSnackbarCallback = Observer<ReaderViewModel.SnackbarText> { text ->
        val root = view
        if (root != null && text != null) {
            val message = getString(text.stringResId, text.params)
            Snackbar.make(root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private val mTtsErrorCallback = Observer<Boolean> { hasTtsError ->
        if (hasTtsError == true) {
            val root = view
            if (root != null) {
                val snackBar = Snackbar.make(root, HtmlCompat.fromHtml(getString(R.string.tts_error)), Snackbar.LENGTH_LONG)
                val intent = Intent("com.android.settings.TTS_SETTINGS")
                if (intent.resolveActivity(root.context.packageManager) != null) {
                    snackBar.setAction(R.string.tts_error_open_system_settings, { _ -> startActivity(intent) })
                } else {
                    snackBar.setAction(R.string.tts_error_open_app_settings, { _ -> startActivity(Intent(context, SettingsActivity::class.java)) })
                }
                snackBar.show()
            }
        }
    }

    private val mPoemFileCallback = Observer<PoemFile> { _ ->
        Log.v(TAG, "invalidateOptionsMenu")
        activity?.invalidateOptionsMenu()
    }

    private val mPlayButtonStateObserver = Observer<ReaderViewModel.PlayButtonState> { playButtonState ->
        Log.v(TAG, "playButtonState $playButtonState")
        updatePlayButton()
        // Sometimes when the tts engine is initialized, the "isSpeaking()" method returns true
        // if you call it immediately.  If we call updatePlayButton only once at this point, we
        // will show a "stop" button instead of a "play" button.  We workaround this by updating
        // the button again after a brief moment, hoping that isSpeaking() will correctly
        // return false, allowing us to display a "play" button.
        mHandler.postDelayed(this::updatePlayButton, 5000)
    }

    inner class ButtonListener {
        fun onPlayButtonClicked() {
            mViewModel.play(mBinding.tvText.text)
        }

        fun onWordCountClicked() {
            childFragmentManager
                    .beginTransaction()
                    .add(HelpDialogFragment.create(R.string.word_count_help_title, R.string.word_count_help_message), DIALOG_TAG)
                    .commit()
        }
    }
}
