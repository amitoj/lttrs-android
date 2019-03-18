package rs.ltt.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import rs.ltt.android.ui.AvatarDrawable;
import rs.ltt.android.R;
import rs.ltt.android.databinding.ThreadOverviewItemBinding;
import rs.ltt.android.entity.ThreadOverviewItem;

public class ThreadOverviewAdapter extends PagedListAdapter<ThreadOverviewItem, ThreadOverviewAdapter.ThreadOverviewViewHolder> {


    public ThreadOverviewAdapter() {
        super(new DiffUtil.ItemCallback<ThreadOverviewItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull ThreadOverviewItem oldItem, @NonNull ThreadOverviewItem newItem) {
                return oldItem.threadId.equals(newItem.threadId);
            }

            @Override
            public boolean areContentsTheSame(@NonNull ThreadOverviewItem oldItem, @NonNull ThreadOverviewItem newItem) {
                return false;
            }
        });
    }

    @NonNull
    @Override
    public ThreadOverviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ThreadOverviewItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.thread_overview_item, parent, false);
        return new ThreadOverviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ThreadOverviewViewHolder holder, int position) {
        final ThreadOverviewItem item = getItem(position);
        if (item == null) {
            return;
        }
        holder.binding.setThread(item);
        Map.Entry<String, ThreadOverviewItem.From> from = item.getFrom();
        if (from == null) {
            return;
        }
        holder.binding.avatar.setImageDrawable(new AvatarDrawable(from.getValue().name, from.getKey()));
    }

    public class ThreadOverviewViewHolder extends RecyclerView.ViewHolder {

        public final ThreadOverviewItemBinding binding;

        public ThreadOverviewViewHolder(@NonNull ThreadOverviewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
