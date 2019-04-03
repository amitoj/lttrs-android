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
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import rs.ltt.android.R;
import rs.ltt.android.databinding.ThreadOverviewItemBinding;
import rs.ltt.android.databinding.ThreadOverviewItemLoadingBinding;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.android.ui.BindingAdapters;
import rs.ltt.android.util.Touch;

public class ThreadOverviewAdapter extends PagedListAdapter<ThreadOverviewItem, ThreadOverviewAdapter.AbstractThreadOverviewViewHolder> {

    private boolean isLoading = false;

    private static final int THREAD_ITEM_VIEW_TYPE = 0;
    private static final int LOADING_ITEM_VIEW_TYPE = 1;

    private OnFlaggedToggled onFlaggedToggled;
    private OnThreadClicked onThreadClicked;


    public ThreadOverviewAdapter() {
        super(new DiffUtil.ItemCallback<ThreadOverviewItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull ThreadOverviewItem oldItem, @NonNull ThreadOverviewItem newItem) {
                return oldItem.threadId.equals(newItem.threadId);
            }

            @Override
            public boolean areContentsTheSame(@NonNull ThreadOverviewItem oldItem, @NonNull ThreadOverviewItem newItem) {
                if (!oldItem.equals(newItem)) {
                    Log.d("lttrs", oldItem.getSubject() + " was not same as new item");
                }
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public AbstractThreadOverviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == THREAD_ITEM_VIEW_TYPE) {
            return new ThreadOverviewViewHolder(DataBindingUtil.inflate(layoutInflater, R.layout.thread_overview_item, parent, false));
        } else {
            return new ThreadOverviewLoadingViewHolder(DataBindingUtil.inflate(layoutInflater, R.layout.thread_overview_item_loading, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractThreadOverviewViewHolder holder, int position) {
        if (holder instanceof ThreadOverviewLoadingViewHolder) {
            ((ThreadOverviewLoadingViewHolder) holder).binding.loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            return;
        }
        if (holder instanceof ThreadOverviewViewHolder) {
            ThreadOverviewViewHolder threadOverviewHolder = (ThreadOverviewViewHolder) holder;
            final ThreadOverviewItem item = getItem(position);
            if (item == null) {
                return;
            }
            threadOverviewHolder.binding.setThread(item);
            threadOverviewHolder.binding.starToggle.setOnClickListener(v -> {
                if (onFlaggedToggled != null) {
                    final boolean target = !item.showAsFlagged();
                    BindingAdapters.setIsFlagged(threadOverviewHolder.binding.starToggle, target);
                    onFlaggedToggled.onFlaggedToggled(item.threadId, target);
                }
            });
            Touch.expandTouchArea(threadOverviewHolder.binding.getRoot(), threadOverviewHolder.binding.starToggle, 16);
            threadOverviewHolder.binding.getRoot().setOnClickListener(v -> {
                if (onThreadClicked != null) {
                    onThreadClicked.onThreadClicked(item.threadId);
                }
            });
        }
    }

    public void setLoading(final boolean loading) {
        final boolean before = this.isLoading;
        this.isLoading = loading;
        if (before != loading) {
            notifyItemChanged(super.getItemCount());
        }
    }

    @Override
    public ThreadOverviewItem getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position < super.getItemCount() ? THREAD_ITEM_VIEW_TYPE : LOADING_ITEM_VIEW_TYPE;
    }

    public void setOnFlaggedToggledListener(OnFlaggedToggled listener) {
        this.onFlaggedToggled = listener;
    }

    public void setOnThreadClickedListener(OnThreadClicked listener) {
        this.onThreadClicked = listener;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }


    abstract class AbstractThreadOverviewViewHolder extends RecyclerView.ViewHolder {

        AbstractThreadOverviewViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class ThreadOverviewViewHolder extends AbstractThreadOverviewViewHolder {

        final public ThreadOverviewItemBinding binding;

        ThreadOverviewViewHolder(@NonNull ThreadOverviewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class ThreadOverviewLoadingViewHolder extends AbstractThreadOverviewViewHolder {

        final ThreadOverviewItemLoadingBinding binding;

        ThreadOverviewLoadingViewHolder(@NonNull ThreadOverviewItemLoadingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnThreadClicked {
        void onThreadClicked(String threadId);
    }
}
