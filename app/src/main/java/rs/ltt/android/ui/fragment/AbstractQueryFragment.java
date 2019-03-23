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

package rs.ltt.android.ui.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import rs.ltt.android.MainNavDirections;
import rs.ltt.android.R;
import rs.ltt.android.databinding.FragmentThreadListBinding;
import rs.ltt.android.ui.adapter.OnFlaggedToggled;
import rs.ltt.android.ui.adapter.ThreadOverviewAdapter;
import rs.ltt.android.ui.model.AbstractQueryViewModel;


public abstract class AbstractQueryFragment extends Fragment implements OnFlaggedToggled, ThreadOverviewAdapter.OnThreadClicked {

    private FragmentThreadListBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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


        //TODO: do we want to get rid of flicer on changes
        //((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        viewModel.isRunningPagingRequest().observe(this, threadOverviewAdapter::setLoading);
        threadOverviewAdapter.setOnFlaggedToggledListener(this);
        threadOverviewAdapter.setOnThreadClickedListener(this);
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_query, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onFlaggedToggled(String threadId, boolean target) {
        getQueryViewModel().toggleFlagged(threadId, target);
    }

    protected abstract AbstractQueryViewModel getQueryViewModel();

    @Override
    public void onThreadClicked(String threadId) {
        final NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navController.navigate(MainNavDirections.actionToThread(threadId));
    }
}
