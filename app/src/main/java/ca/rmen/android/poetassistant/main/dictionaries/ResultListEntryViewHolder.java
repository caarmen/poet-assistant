package ca.rmen.android.poetassistant.main.dictionaries;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

public class ResultListEntryViewHolder extends RecyclerView.ViewHolder {

    public ViewDataBinding binding;

    public ResultListEntryViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
