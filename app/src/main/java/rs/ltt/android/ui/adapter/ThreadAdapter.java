package rs.ltt.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import rs.ltt.android.R;
import rs.ltt.android.databinding.EmailItemBinding;
import rs.ltt.android.entity.FullEmail;

public class ThreadAdapter extends PagedListAdapter<FullEmail, ThreadAdapter.ThreadItemViewHolder> {

    public ThreadAdapter() {
        super(new DiffUtil.ItemCallback<FullEmail>() {
            @Override
            public boolean areItemsTheSame(@NonNull FullEmail oldItem, @NonNull FullEmail newItem) {
                return oldItem.id.equals(newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull FullEmail oldItem, @NonNull FullEmail newItem) {
                return false;
            }
        });
    }

    @NonNull
    @Override
    public ThreadItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ThreadItemViewHolder(DataBindingUtil.inflate(inflater, R.layout.email_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull ThreadItemViewHolder holder, int position) {
        FullEmail email = getItem(position);
        holder.binding.setEmail(email);

    }



    class ThreadItemViewHolder extends RecyclerView.ViewHolder {

        private final EmailItemBinding binding;

        public ThreadItemViewHolder(@NonNull EmailItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


}
