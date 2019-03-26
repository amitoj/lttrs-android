/*
 * Copyright 2019 Daniel Gultsch
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import rs.ltt.android.R;
import rs.ltt.android.databinding.MailboxListHeaderBinding;
import rs.ltt.android.databinding.MailboxListItemBinding;
import rs.ltt.android.entity.MailboxOverviewItem;

public class MailboxListAdapter extends RecyclerView.Adapter<MailboxListAdapter.AbstractMailboxViewHolder> {

    private static final int ITEM_VIEW_TYPE = 1;
    private static final int HEADER_VIEW_TYPE = 2;

    private static final DiffUtil.ItemCallback<MailboxOverviewItem> ITEM_CALLBACK = new DiffUtil.ItemCallback<MailboxOverviewItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull MailboxOverviewItem oldItem, @NonNull MailboxOverviewItem newItem) {
            return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull MailboxOverviewItem oldItem, @NonNull MailboxOverviewItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    private final AsyncListDiffer<MailboxOverviewItem> mDiffer =  new AsyncListDiffer<>(new OffsetListUpdateCallback<>(this,1), new AsyncDifferConfig.Builder<>(ITEM_CALLBACK).build());

    private String selectedId = null;

    private OnMailboxOverviewItemSelected onMailboxOverviewItemSelected = null;

    public MailboxListAdapter() {
        super();
    }


    @NonNull
    @Override
    public AbstractMailboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE) {
            MailboxListItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.mailbox_list_item, parent, false);
            return new MailboxViewHolder(binding);
        } else {
            MailboxListHeaderBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.mailbox_list_header, parent, false);
            return new MailboxHeaderViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractMailboxViewHolder abstractHolder, final int position) {
        if (abstractHolder instanceof MailboxHeaderViewHolder) {
            return;
        }
        MailboxViewHolder holder = (MailboxViewHolder) abstractHolder;
        final Context context = holder.binding.getRoot().getContext();
        final MailboxOverviewItem mailbox = getItem(position);
        holder.binding.setMailbox(mailbox);
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

    public String getSelectedId() {
        return this.selectedId;
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

    private int getPosition(final String id) {
        if (id == null) {
            return RecyclerView.NO_POSITION;
        }
        List<MailboxOverviewItem> items = mDiffer.getCurrentList();
        for (int i = 0; i < items.size(); ++i) {
            if (id.equals(items.get(i).id)) {
                return i + 1;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ?  HEADER_VIEW_TYPE : ITEM_VIEW_TYPE;
    }

    private MailboxOverviewItem getItem(int position) {
        return this.mDiffer.getCurrentList().get(position - 1);
    }

    public void submitList(List<MailboxOverviewItem> items) {
        this.mDiffer.submitList(items);
    }

    public void setOnMailboxOverviewItemSelectedListener(OnMailboxOverviewItemSelected listener) {
        this.onMailboxOverviewItemSelected = listener;
    }

    public interface OnMailboxOverviewItemSelected {
        void onMailboxOverviewItemSelected(MailboxOverviewItem mailboxOverviewItem);
    }

    class AbstractMailboxViewHolder extends RecyclerView.ViewHolder {

        public AbstractMailboxViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class MailboxViewHolder extends AbstractMailboxViewHolder {

        private final MailboxListItemBinding binding;

        MailboxViewHolder(@NonNull MailboxListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    class MailboxHeaderViewHolder extends AbstractMailboxViewHolder {

        private final MailboxListHeaderBinding binding;

        MailboxHeaderViewHolder(@NonNull MailboxListHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
