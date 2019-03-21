package rs.ltt.android.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import rs.ltt.android.R;
import rs.ltt.android.databinding.FragmentThreadBinding;
import rs.ltt.android.ui.adapter.ThreadAdapter;
import rs.ltt.android.ui.model.ThreadViewModel;
import rs.ltt.android.ui.model.ThreadViewModelFactory;

public class ThreadFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final String threadId = ThreadFragmentArgs.fromBundle(getArguments()).getThread();
        final ThreadViewModel viewModel = ViewModelProviders.of(this, new ThreadViewModelFactory(getActivity().getApplication(), threadId)).get(ThreadViewModel.class);
        FragmentThreadBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_thread, container, false);
        ThreadAdapter threadAdapter = new ThreadAdapter();
        binding.list.setAdapter(threadAdapter);
        viewModel.getEmails().observe(this, threadAdapter::submitList);
        return binding.getRoot();
    }
}
