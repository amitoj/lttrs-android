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
