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
import androidx.lifecycle.ViewModelProviders;
import rs.ltt.android.R;
import rs.ltt.android.databinding.FragmentThreadBinding;
import rs.ltt.android.ui.adapter.OnFlaggedToggled;
import rs.ltt.android.ui.adapter.ThreadAdapter;
import rs.ltt.android.ui.model.ThreadViewModel;
import rs.ltt.android.ui.model.ThreadViewModelFactory;

public class ThreadFragment extends Fragment implements OnFlaggedToggled {

    private ThreadViewModel threadViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final String threadId = ThreadFragmentArgs.fromBundle(getArguments()).getThread();
        threadViewModel = ViewModelProviders.of(this, new ThreadViewModelFactory(getActivity().getApplication(), threadId)).get(ThreadViewModel.class);
        FragmentThreadBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_thread, container, false);
        ThreadAdapter threadAdapter = new ThreadAdapter();
        binding.list.setAdapter(threadAdapter);
        threadViewModel.getEmails().observe(this, threadAdapter::submitList);
        threadViewModel.getHeader().observe(this, threadAdapter::setThreadHeader);
        threadAdapter.setOnFlaggedToggledListener(this);
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_thread, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onFlaggedToggled(String threadId, boolean target) {
        threadViewModel.toggleFlagged(threadId, target);
    }
}
