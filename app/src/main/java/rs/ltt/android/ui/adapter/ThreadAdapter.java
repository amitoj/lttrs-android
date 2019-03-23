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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;
import rs.ltt.android.R;
import rs.ltt.android.databinding.EmailHeaderBinding;
import rs.ltt.android.databinding.EmailItemBinding;
import rs.ltt.android.entity.FullEmail;
import rs.ltt.android.entity.ThreadHeader;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.android.util.Touch;

public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.AbstractThreadItemViewHolder> {

    private static final DiffUtil.ItemCallback<FullEmail> ITEM_CALLBACK = new DiffUtil.ItemCallback<FullEmail>() {

        @Override
        public boolean areItemsTheSame(@NonNull FullEmail oldItem, @NonNull FullEmail newItem) {
            return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull FullEmail oldItem, @NonNull FullEmail newItem) {
            return false;
        }
    };

    private static final int ITEM_VIEW_TYPE = 1;
    private static final int HEADER_VIEW_TYPE = 2;

    private ThreadHeader threadHeader;

    private OnFlaggedToggled onFlaggedToggled;


    //we need this rather inconvenient setup instead of simply using PagedListAdapter to allow for
    //a header view. If we were to use the PagedListAdapter the item update callbacks wouldn’t work.
    //The problem and the solution is described in this github issue: https://github.com/googlesamples/android-architecture-components/issues/375
    //additional documentation on how to implement a AsyncPagedListDiffer can be found here:
    //https://developer.android.com/reference/android/arch/paging/AsyncPagedListDiffer
    private final AdapterListUpdateCallback adapterCallback = new AdapterListUpdateCallback(this);
    private final AsyncPagedListDiffer<FullEmail> mDiffer = new AsyncPagedListDiffer<>(new ListUpdateCallback() {
        @Override
        public void onInserted(int position, int count) {
            adapterCallback.onInserted(position + 1, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            adapterCallback.onRemoved(position + 1, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            adapterCallback.onMoved(fromPosition + 1, toPosition + 1);
        }

        @Override
        public void onChanged(int position, int count, @Nullable Object payload) {
            adapterCallback.onChanged(position + 1, count, payload);
        }
    }, new AsyncDifferConfig.Builder<>(ITEM_CALLBACK).build());

    @NonNull
    @Override
    public AbstractThreadItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ITEM_VIEW_TYPE) {
            return new ThreadItemViewHolder(DataBindingUtil.inflate(inflater, R.layout.email_item, parent, false));
        } else {
            return new ThreadHeaderViewHolder(DataBindingUtil.inflate(inflater, R.layout.email_header, parent, false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull AbstractThreadItemViewHolder holder, int position) {
        if (holder instanceof ThreadHeaderViewHolder) {
            ThreadHeaderViewHolder headerViewHolder = (ThreadHeaderViewHolder) holder;
            headerViewHolder.binding.setHeader(threadHeader);
            headerViewHolder.binding.starToggle.setOnClickListener(v -> {
                if (onFlaggedToggled != null && threadHeader != null) {
                    final boolean target = !threadHeader.showAsFlagged();
                    ThreadOverviewItem.setIsFlagged(headerViewHolder.binding.starToggle, target);
                    onFlaggedToggled.onFlaggedToggled(threadHeader.threadId, target);
                }
            });
            Touch.expandTouchArea(headerViewHolder.binding.getRoot(), headerViewHolder.binding.starToggle, 16);
        } else if (holder instanceof ThreadItemViewHolder) {
            ThreadItemViewHolder itemViewHolder = (ThreadItemViewHolder) holder;
            FullEmail email = mDiffer.getItem(position - 1);
            final boolean lastEmail = mDiffer.getItemCount() == position;
            itemViewHolder.binding.setEmail(email);
            itemViewHolder.binding.divider.setVisibility(lastEmail ? View.GONE : View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mDiffer.getItemCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? HEADER_VIEW_TYPE : ITEM_VIEW_TYPE;
    }

    public void setThreadHeader(ThreadHeader threadHeader) {
        this.threadHeader = threadHeader;
        //TODO notify only if actually changed
        notifyItemChanged(0);
    }

    public void setOnFlaggedToggledListener(OnFlaggedToggled listener) {
        this.onFlaggedToggled = listener;
    }

    public void submitList(PagedList<FullEmail> pagedList) {
        mDiffer.submitList(pagedList);
    }

    class AbstractThreadItemViewHolder extends RecyclerView.ViewHolder {


        AbstractThreadItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class ThreadItemViewHolder extends AbstractThreadItemViewHolder {

        private final EmailItemBinding binding;

        ThreadItemViewHolder(@NonNull EmailItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    class ThreadHeaderViewHolder extends AbstractThreadItemViewHolder {

        private final EmailHeaderBinding binding;

        ThreadHeaderViewHolder(@NonNull EmailHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


}
