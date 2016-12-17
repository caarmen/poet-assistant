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

package ca.rmen.android.poetassistant.main;

import android.text.TextUtils;
import android.widget.PopupMenu;
import android.widget.TextView;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.dictionaries.rt.OnWordClickListener;

public class TextPopupMenu {

    /**
     * Create a popup menu allowing the user to lookup the selected word in the given text view, in
     * the rhymer, thesaurus, or dictionary.
     *
     * @param textView if the text view has selected text, tapping on it will bring up the popup menu
     * @param listener this listener will be notified when the user selects one of the popup menu items
     */
    public static void createPopupMenu(final TextView textView, final OnWordClickListener listener) {
        textView.setOnLongClickListener(v -> {
            final String selectedWord = getSelectedWord(textView);
            if (TextUtils.isEmpty(selectedWord)) return false;
            PopupMenu popupMenu = new PopupMenu(textView.getContext(), textView);
            popupMenu.inflate(R.menu.menu_word_lookup);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_lookup_rhymer) {
                    listener.onWordClick(selectedWord, Tab.RHYMER);
                } else if (item.getItemId() == R.id.action_lookup_thesaurus) {
                    listener.onWordClick(selectedWord, Tab.THESAURUS);
                } else if (item.getItemId() == R.id.action_lookup_dictionary) {
                    listener.onWordClick(selectedWord, Tab.DICTIONARY);
                }
                return false;
            });
            popupMenu.show();
            return true;
        });
    }

    private static String getSelectedWord(TextView textView) {
        int selectionStart = textView.getSelectionStart();
        int selectionEnd = textView.getSelectionEnd();
        String text = textView.getText().toString();

        // The user selected some text, use that (even if it contains partial words)
        if (selectionStart < selectionEnd) return text.substring(selectionStart, selectionEnd);

        // Cursor at the end of the text: nothing selected
        if (selectionStart == text.length()) return null;

        // Cursor at the beginning of the text: nothing selected
        if (selectionStart == 0 && selectionEnd == 0) return null;

        // The cursor is in the middle of a word. Find the whole word.
        int wordBegin;
        for (wordBegin = selectionStart; wordBegin >= 0; wordBegin--) {
            if (!Character.isLetter(text.charAt(wordBegin))) {
                break;
            }
        }
        wordBegin++;
        int wordEnd;
        for (wordEnd = selectionEnd; wordEnd < text.length(); wordEnd++) {
            if (!Character.isLetter(text.charAt(wordEnd))) {
                break;
            }
        }

        if (wordBegin < wordEnd) return text.substring(wordBegin, wordEnd);
        return null;
    }


}
