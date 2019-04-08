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

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import rs.ltt.android.MainNavigationDirections;
import rs.ltt.android.R;
import rs.ltt.android.entity.MailboxOverviewItem;
import rs.ltt.android.entity.ThreadOverviewItem;

public class MailboxQueryFragment extends AbstractMailboxQueryFragment {
    @Override
    protected String getMailboxId() {
        final Bundle bundle = getArguments();
        return MailboxQueryFragmentArgs.fromBundle(bundle == null ? new Bundle() : bundle).getMailbox();
    }

    @Override
    public void onThreadClicked(String threadId) {
        MailboxOverviewItem mailbox = mailboxQueryViewModel.getMailbox().getValue();
        final NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        final String label = mailbox != null && mailbox.role == null ? mailbox.name : null;
        navController.navigate(MainNavigationDirections.actionToThread(threadId, label));
    }

    @Override
    protected void onQueryItemSwiped(ThreadOverviewItem item) {
        mailboxQueryViewModel.removeFromMailbox(item);
    }
}
