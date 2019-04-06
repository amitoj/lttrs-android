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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import rs.ltt.android.Credentials;
import rs.ltt.android.MainNavDirections;
import rs.ltt.android.R;
import rs.ltt.android.database.LttrsDatabase;
import rs.ltt.android.databinding.ActivityMainBinding;
import rs.ltt.android.entity.MailboxOverviewItem;
import rs.ltt.android.entity.SearchSuggestionEntity;
import rs.ltt.android.ui.adapter.MailboxListAdapter;
import rs.ltt.android.ui.fragment.AbstractMailboxQueryFragment;
import rs.ltt.android.ui.fragment.MailboxQueryFragmentDirections;
import rs.ltt.android.ui.fragment.MainMailboxQueryFragmentDirections;
import rs.ltt.android.ui.fragment.SearchQueryFragment;
import rs.ltt.android.ui.model.MailboxListViewModel;
import rs.ltt.jmap.common.entity.Role;

public class MainActivity extends AppCompatActivity implements AbstractMailboxQueryFragment.OnMailboxOpened, SearchQueryFragment.OnTermSearched, NavController.OnDestinationChangedListener, MenuItem.OnActionExpandListener {

    private static final int NUM_TOOLBAR_ICON = 1;

    final MailboxListAdapter mailboxListAdapter = new MailboxListAdapter();

    private ActivityMainBinding binding = null;
    private SearchView mSearchView;

    private static final List<Integer> MAIN_DESTINATIONS = Arrays.asList(
            R.id.inbox,
            R.id.mailbox
    );

    private String currentSearchTerm = null;

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
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        final int currentDestination = getCurrentDestinationId();
        final boolean showSearch = MAIN_DESTINATIONS.contains(currentDestination) || currentDestination == R.id.search;

        getMenuInflater().inflate(R.menu.activity_main, menu);

        MenuItem mSearchItem = menu.findItem(R.id.action_search);

        mSearchItem.setVisible(showSearch);

        if (showSearch) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            mSearchView = (SearchView) mSearchItem.getActionView();
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            if (currentDestination == R.id.search) {
                setSearchToolbarColors();
                mSearchItem.expandActionView();
                mSearchView.setQuery(currentSearchTerm, false);
                mSearchView.clearFocus();
            }
            mSearchItem.setOnActionExpandListener(this);
        } else {
            mSearchView = null;
            resetToolbarColors();
        }

        return super.onCreateOptionsMenu(menu);
    }

    private int getCurrentDestinationId() {
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        final NavDestination currentDestination = navController.getCurrentDestination();
        return currentDestination == null ? 0 : currentDestination.getId();
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
        final boolean showMenu = MAIN_DESTINATIONS.contains(destinationId);
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
                if (currentDestination != null && MAIN_DESTINATIONS.contains(currentDestination.getId())) {
                    binding.drawerLayout.openDrawer(GravityCompat.START);
                    return true;
                } else {
                    onBackPressed();
                }
        }
        return super.onOptionsItemSelected(item);

    }

    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            if (mSearchView != null) {
                mSearchView.setQuery(query,false);
                mSearchView.clearFocus(); //this does not work on all phones / android versions; therefor we have this followed by a requestFocus() on the list
            }
            binding.mailboxList.requestFocus();

            new Thread(() -> LttrsDatabase.getInstance(this, Credentials.username).searchSuggestionDao().insert(SearchSuggestionEntity.of(query))).start();
            final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.navigate(MainNavDirections.actionSearch(query));
        }

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

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        animateShowSearchToolbar();
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        animateCloseSearchToolbar();
        if (getCurrentDestinationId() == R.id.search) {
            final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.navigateUp();
        }
        return true;
    }

    public void animateShowSearchToolbar() {
        setSearchToolbarColors();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateShowSearchToolbarLollipop();
        } else {
            animateShowSearchToolbarLegacy();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateShowSearchToolbarLollipop() {
        final int toolbarIconWidth = getResources().getDimensionPixelSize(R.dimen.toolbar_icon_width);
        final int width = binding.toolbar.getWidth() - ((toolbarIconWidth * NUM_TOOLBAR_ICON) / 2);
        Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(binding.toolbar, Theme.isRtl(this) ? binding.toolbar.getWidth() - width : width, binding.toolbar.getHeight() / 2, 0.0f, (float) width);
        createCircularReveal.setDuration(250);
        createCircularReveal.start();
    }

    private void animateShowSearchToolbarLegacy() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, (float) (-binding.toolbar.getHeight()), 0.0f);
        translateAnimation.setDuration(220);
        binding.toolbar.clearAnimation();
        binding.toolbar.startAnimation(translateAnimation);
    }

    public void animateCloseSearchToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateCloseSearchToolbarLollipop();
        } else {
            animateCloseSearchToolbarLegacy();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateCloseSearchToolbarLollipop() {
        final int toolbarIconWidth = getResources().getDimensionPixelSize(R.dimen.toolbar_icon_width);
        final int width = binding.toolbar.getWidth() - ((toolbarIconWidth * NUM_TOOLBAR_ICON) / 2);
        Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(binding.toolbar, Theme.isRtl(this) ? binding.toolbar.getWidth() - width : width, binding.toolbar.getHeight() / 2, (float) width, 0.0f);
        createCircularReveal.setDuration(250);
        createCircularReveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                resetToolbarColors();
            }
        });
        createCircularReveal.start();
    }

    private void animateCloseSearchToolbarLegacy() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        Animation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-binding.toolbar.getHeight()));
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(translateAnimation);
        animationSet.setDuration(220);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                resetToolbarColors();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        binding.toolbar.startAnimation(animationSet);
    }

    private void resetToolbarColors() {
        binding.toolbar.setBackgroundColor(Theme.getColor(MainActivity.this, R.attr.colorPrimary));
        binding.drawerLayout.setStatusBarBackgroundColor(Theme.getColor(MainActivity.this, R.attr.colorPrimaryDark));
    }

    private void setSearchToolbarColors() {
        binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        binding.drawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.quantum_grey_600));
    }

    @Override
    public void onTermSearched(String term) {
        this.currentSearchTerm = term;
        Log.d("lttrs","on term searched "+term);
    }
}
