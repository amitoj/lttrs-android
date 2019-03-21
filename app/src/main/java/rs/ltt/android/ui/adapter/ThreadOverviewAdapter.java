package rs.ltt.android.ui.adapter;

import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import rs.ltt.android.R;
import rs.ltt.android.databinding.ThreadOverviewItemBinding;
import rs.ltt.android.databinding.ThreadOverviewItemLoadingBinding;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.android.ui.AvatarDrawable;

public class ThreadOverviewAdapter extends PagedListAdapter<ThreadOverviewItem, ThreadOverviewAdapter.AbstractThreadOverviewViewHolder> {

    private boolean isLoading = false;

    private static final int THREAD_ITEM_VIEW_TYPE = 0;
    private static final int LOADING_ITEM_VIEW_TYPE = 1;

    private OnFlaggedToggled onFlaggedToggled;


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
            Map.Entry<String, ThreadOverviewItem.From> from = item.getFrom();
            if (from == null) {
                return;
            }
            threadOverviewHolder.binding.avatar.setImageDrawable(new AvatarDrawable(from.getValue().name, from.getKey()));
            threadOverviewHolder.binding.starToggle.setOnClickListener(v -> {
                if (onFlaggedToggled != null) {
                    ThreadOverviewItem.setIsFlagged(threadOverviewHolder.binding.starToggle, !item.showAsFlagged());
                    onFlaggedToggled.onFlaggedToggled(item);
                }
            });
            expandTouchArea(threadOverviewHolder.binding.getRoot(),threadOverviewHolder.binding.starToggle,16);
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
    public int getItemViewType(int position) {
        return position < super.getItemCount() ? THREAD_ITEM_VIEW_TYPE : LOADING_ITEM_VIEW_TYPE;
    }

    public void setOnFlaggedToggledListener(OnFlaggedToggled listener) {
        this.onFlaggedToggled = listener;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }


    private static void expandTouchArea(final View parent, final View view, final int dp) {
        float scale = parent.getContext().getResources().getDisplayMetrics().density;
        int padding = (int) (scale * dp);
        parent.post(() -> {
            Rect rect = new Rect();
            view.getHitRect(rect);
            rect.top -= padding;
            rect.left -= padding;
            rect.right += padding;
            rect.bottom += padding;
            parent.setTouchDelegate(new TouchDelegate(rect, view));
        });
    }

    abstract class AbstractThreadOverviewViewHolder extends RecyclerView.ViewHolder {

        AbstractThreadOverviewViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class ThreadOverviewViewHolder extends AbstractThreadOverviewViewHolder {

        final ThreadOverviewItemBinding binding;

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

    public interface OnFlaggedToggled {
        void onFlaggedToggled(ThreadOverviewItem threadOverviewItem);
    }
}
