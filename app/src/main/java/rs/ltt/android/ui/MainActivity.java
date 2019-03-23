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

package rs.ltt.android.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavArgument;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import rs.ltt.android.R;
import rs.ltt.android.ui.adapter.MailboxListAdapter;
import rs.ltt.android.databinding.ActivityMainBinding;
import rs.ltt.android.entity.MailboxOverviewItem;
import rs.ltt.android.ui.fragment.AbstractMailboxQueryFragment;
import rs.ltt.android.ui.fragment.MailboxQueryFragmentDirections;
import rs.ltt.android.ui.fragment.MainMailboxQueryFragmentDirections;
import rs.ltt.android.ui.model.MailboxListViewModel;
import rs.ltt.jmap.common.entity.Role;

public class MainActivity extends AppCompatActivity implements AbstractMailboxQueryFragment.OnMailboxOpened, NavController.OnDestinationChangedListener {

    final MailboxListAdapter mailboxListAdapter = new MailboxListAdapter();
    private ActivityMainBinding binding = null;

    private static final List<Integer> DESTINATIONS_SHOWING_DRAWER_BUTTON = Arrays.asList(
            R.id.inbox,
            R.id.mailbox
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        MailboxListViewModel mailboxListViewModel = ViewModelProviders.of(this).get(MailboxListViewModel.class);
        setSupportActionBar(binding.toolbar);

        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        mailboxListAdapter.setOnMailboxOverviewItemSelectedListener(mailboxOverviewItem -> {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            final boolean navigateToInbox = mailboxOverviewItem.role == Role.INBOX;
            NavDestination currentDestination = navController.getCurrentDestination();
            if (currentDestination != null && currentDestination.getId() == R.id.inbox) {
                if (navigateToInbox) {
                    return;
                }
                navController.navigate(MainMailboxQueryFragmentDirections.actionInboxToMailbox(mailboxOverviewItem.id));
            } else if (currentDestination != null && currentDestination.getId() == R.id.mailbox) {
                if (navigateToInbox) {
                    navController.navigate(MailboxQueryFragmentDirections.actionMailboxToInbox());
                } else {
                    if (mailboxOverviewItem.id.equals(mailboxListAdapter.getSelectedId())) {
                        return;
                    }
                    navController.navigate(MailboxQueryFragmentDirections.actionMailboxToMailbox(mailboxOverviewItem.id));
                }
            }
            //currently unused should remain here in case we bring scrollable toolbar back
            binding.appBarLayout.setExpanded(true, false);
        });
        binding.mailboxList.setAdapter(mailboxListAdapter);
        mailboxListViewModel.getMailboxes().observe(this, mailboxListAdapter::submitList);
    }

    @Override
    public void onStart() {
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.removeOnDestinationChangedListener(this);
    }

    private void configureActionBarForDestination(NavDestination destination) {
        final ActionBar actionbar = getSupportActionBar();
        if (actionbar == null) {
            return;
        }
        final int destinationId = destination.getId();
        final boolean showMenu = DESTINATIONS_SHOWING_DRAWER_BUTTON.contains(destinationId);
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(showMenu ? R.drawable.ic_menu_black_24dp : R.drawable.ic_arrow_back_white_24dp);
        actionbar.setDisplayShowTitleEnabled(destinationId != R.id.thread);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                final NavDestination currentDestination = navController.getCurrentDestination();
                if (currentDestination != null && DESTINATIONS_SHOWING_DRAWER_BUTTON.contains(currentDestination.getId())) {
                    binding.drawerLayout.openDrawer(GravityCompat.START);
                    return true;
                } else {
                    onBackPressed();
                }
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onMailboxOpened(MailboxOverviewItem mailboxOverviewItem) {
        setTitle(mailboxOverviewItem.name);
        mailboxListAdapter.setSelectedId(mailboxOverviewItem.id);
    }

    @Override
    public void onBackPressed() {
        if (binding != null && binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        configureActionBarForDestination(destination);
    }
}
