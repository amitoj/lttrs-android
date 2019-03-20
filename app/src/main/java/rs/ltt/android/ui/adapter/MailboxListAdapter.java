package rs.ltt.android.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import rs.ltt.android.R;
import rs.ltt.android.databinding.MailboxListItemBinding;
import rs.ltt.android.entity.MailboxOverviewItem;

public class MailboxListAdapter extends ListAdapter<MailboxOverviewItem, MailboxListAdapter.MailboxViewHolder> {

    private String selectedId = null;

    private OnMailboxOverviewItemSelected onMailboxOverviewItemSelected = null;

    public MailboxListAdapter() {
        super(new DiffUtil.ItemCallback<MailboxOverviewItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull MailboxOverviewItem oldItem, @NonNull MailboxOverviewItem newItem) {
                return oldItem.id.equals(newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull MailboxOverviewItem oldItem, @NonNull MailboxOverviewItem newItem) {
                return oldItem.equals(newItem);
            }
        });
    }


    @NonNull
    @Override
    public MailboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MailboxListItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.mailbox_list_item, parent, false);
        return new MailboxViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MailboxViewHolder holder, final int position) {
        final Context context = holder.binding.getRoot().getContext();
        final MailboxOverviewItem mailbox = getItem(position);
        holder.binding.setMailbox(mailbox);
        holder.binding.cover.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        holder.binding.item.setOnClickListener(v -> {
            if (onMailboxOverviewItemSelected != null) {
                onMailboxOverviewItemSelected.onMailboxOverviewItemSelected(mailbox);
            }
        });
        if (mailbox.id.equals(this.selectedId)) {
            holder.binding.item.setBackgroundColor(ContextCompat.getColor(context, R.color.primary12));
            ImageViewCompat.setImageTintList(holder.binding.icon, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));
        } else {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            holder.binding.item.setBackgroundResource(outValue.resourceId);
            ImageViewCompat.setImageTintList(holder.binding.icon, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black54)));
        }
    }

    public void setSelectedId(final String id) {
        if ((id == null && this.selectedId == null) || (id != null && id.equals(this.selectedId))) {
            return;
        }
        final int previous = getPosition(this.selectedId);
        final int position = getPosition(id);
        this.selectedId = id;
        if (previous != RecyclerView.NO_POSITION) {
            notifyItemChanged(previous);
        }
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position);
        }
    }

    public String getSelectedId() {
        return this.selectedId;
    }

    private int getPosition(final String id) {
        if (id == null) {
            return RecyclerView.NO_POSITION;
        }
        List<MailboxOverviewItem> items = getCurrentList();
        for (int i = 0; i < items.size(); ++i) {
            if (id.equals(items.get(i).id)) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    public void setOnMailboxOverviewItemSelectedListener(OnMailboxOverviewItemSelected listener) {
        this.onMailboxOverviewItemSelected = listener;
    }

    class MailboxViewHolder extends RecyclerView.ViewHolder {

        private final MailboxListItemBinding binding;

        MailboxViewHolder(@NonNull MailboxListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnMailboxOverviewItemSelected {
        void onMailboxOverviewItemSelected(MailboxOverviewItem mailboxOverviewItem);
    }
}
