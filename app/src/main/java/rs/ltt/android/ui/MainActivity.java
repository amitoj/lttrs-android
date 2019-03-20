package rs.ltt.android.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
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

public class MainActivity extends AppCompatActivity implements AbstractMailboxQueryFragment.OnMailboxOpened {

    final MailboxListAdapter mailboxListAdapter = new MailboxListAdapter();
    private ActivityMainBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        MailboxListViewModel mailboxListViewModel = ViewModelProviders.of(this).get(MailboxListViewModel.class);
        setSupportActionBar(binding.toolbar);
        final ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        }

        mailboxListAdapter.setOnMailboxOverviewItemSelectedListener(mailboxOverviewItem -> {
            //might take a moment for the fragment to load and emit the id so we set it here
            //will be overwritten by onMailboxOpened promptly
            //but it is also a bit expensive
            //mailboxListAdapter.setSelectedId(mailboxOverviewItem.id);

            binding.drawerLayout.closeDrawer(GravityCompat.START);
            binding.appBarLayout.setExpanded(true, false);
            final boolean navigateToInbox = mailboxOverviewItem.role == Role.INBOX;
            final NavController nabController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavDestination currentDestination = nabController.getCurrentDestination();
            if (currentDestination != null && currentDestination.getId() == R.id.inbox) {
                if (navigateToInbox) {
                    return;
                }
                nabController.navigate(MainMailboxQueryFragmentDirections.actionInboxToMailbox(mailboxOverviewItem.id));
            } else if (currentDestination != null && currentDestination.getId() == R.id.mailbox) {
                if (navigateToInbox) {
                    nabController.navigate(MailboxQueryFragmentDirections.actionMailboxToInbox());
                } else {
                    //todo check if we already are in proper fragment and ignore if so
                    nabController.navigate(MailboxQueryFragmentDirections.actionMailboxToMailbox(mailboxOverviewItem.id));
                }
            }
            //navigate to this mailbox
        });
        binding.mailboxList.setAdapter(mailboxListAdapter);
        mailboxListViewModel.getMailboxes().observe(this, mailboxListAdapter::submitList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                binding.drawerLayout.openDrawer(GravityCompat.START);
                return true;
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
}
