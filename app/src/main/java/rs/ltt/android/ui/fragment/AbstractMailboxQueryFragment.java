package rs.ltt.android.ui.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import rs.ltt.android.entity.MailboxOverviewItem;
import rs.ltt.android.ui.model.AbstractQueryViewModel;
import rs.ltt.android.ui.model.MailboxQueryViewModel;
import rs.ltt.android.ui.model.MailboxViewModelFactory;


public abstract class AbstractMailboxQueryFragment extends AbstractQueryFragment {

    private MailboxQueryViewModel mailboxQueryViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mailboxQueryViewModel = ViewModelProviders.of(this, new MailboxViewModelFactory(getActivity().getApplication(), getMailboxId())).get(MailboxQueryViewModel.class);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected AbstractQueryViewModel getQueryViewModel() {
        return this.mailboxQueryViewModel;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mailboxQueryViewModel.getMailbox().observe(this, mailboxOverviewItem -> {
            if (mailboxOverviewItem == null) {
                return;
            }
            final Activity activity = getActivity();
            if (activity instanceof OnMailboxOpened) {
                ((OnMailboxOpened) activity).onMailboxOpened(mailboxOverviewItem);
            }
        });
    }

    public interface OnMailboxOpened {
        void onMailboxOpened(MailboxOverviewItem mailboxOverviewItem);
    }

    protected abstract String getMailboxId();

}
