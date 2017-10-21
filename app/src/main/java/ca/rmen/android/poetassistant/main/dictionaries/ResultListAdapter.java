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

package ca.rmen.android.poetassistant.main.dictionaries;

import android.arch.paging.PagedListAdapter;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.DiffCallback;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import ca.rmen.android.poetassistant.Constants;

public abstract class ResultListAdapter<T> extends PagedListAdapter<T, ResultListAdapter.ResultListEntryViewHolder> {

    private static final String TAG = Constants.TAG + ResultListAdapter.class.getSimpleName();

    protected ResultListAdapter() {
        super(new ResultListDiffCallback<>());
    }

    public static class ResultListEntryViewHolder extends RecyclerView.ViewHolder {

        public final ViewDataBinding binding;

        public ResultListEntryViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static class ResultListDiffCallback<T> extends DiffCallback<T> {
        @Override
        public boolean areItemsTheSame(@NonNull T oldEntry, @NonNull T newEntry) {
            Log.v(TAG, "areItemsTheSame: " + oldEntry + ", " + newEntry);
            return oldEntry.equals(newEntry);
        }

        @Override
        public boolean areContentsTheSame(@NonNull T oldEntry, @NonNull T newEntry) {
            Log.v(TAG, "areContentsTheSame: " + oldEntry + ", " + newEntry);
            return oldEntry.equals(newEntry);
        }
    }

}
