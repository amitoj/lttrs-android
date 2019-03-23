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


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import rs.ltt.android.entity.MailboxOverviewItem;
import rs.ltt.android.entity.ThreadOverviewItem;
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

    @Override
    protected boolean onQueryItemSwipe(ThreadOverviewItem item) {
        return true;
    }

    @Override
    protected void onQueryItemSwiped(ThreadOverviewItem item) {
        mailboxQueryViewModel.archive(item);

    }

    public interface OnMailboxOpened {
        void onMailboxOpened(MailboxOverviewItem mailboxOverviewItem);
    }

    protected abstract String getMailboxId();

}
