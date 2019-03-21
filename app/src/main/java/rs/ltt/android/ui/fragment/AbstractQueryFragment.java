package rs.ltt.android.ui.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import rs.ltt.android.R;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.android.ui.adapter.ThreadOverviewAdapter;
import rs.ltt.android.databinding.FragmentThreadListBinding;
import rs.ltt.android.ui.model.AbstractQueryViewModel;


public abstract class AbstractQueryFragment extends Fragment implements ThreadOverviewAdapter.OnFlaggedToggled {

    private FragmentThreadListBinding binding;

    public AbstractQueryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final AbstractQueryViewModel viewModel = getQueryViewModel();
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_thread_list, container, false);
        final ThreadOverviewAdapter threadOverviewAdapter = new ThreadOverviewAdapter();
        viewModel.getThreadOverviewItems().observe(this, threadOverviewItems -> {
            final RecyclerView.LayoutManager layoutManager = binding.threadList.getLayoutManager();
            final boolean atTop;
            if (layoutManager instanceof LinearLayoutManager) {
                atTop = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition() == 0;
            } else {
                atTop = false;
            }
            threadOverviewAdapter.submitList(threadOverviewItems, () -> {
                if (atTop) {
                    binding.threadList.scrollToPosition(0);
                }
            });
        });
        binding.threadList.setAdapter(threadOverviewAdapter);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        viewModel.isRunningPagingRequest().observe(this, threadOverviewAdapter::setLoading);
        threadOverviewAdapter.setOnFlaggedToggledListener(this);
        return binding.getRoot();
    }

    @Override
    public void onFlaggedToggled(ThreadOverviewItem item, boolean target) {
        getQueryViewModel().toggleFlagged(item, target);

    }

    protected abstract AbstractQueryViewModel getQueryViewModel();

}
