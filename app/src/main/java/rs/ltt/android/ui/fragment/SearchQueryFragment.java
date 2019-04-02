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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.android.ui.QueryItemTouchHelper;
import rs.ltt.android.ui.model.AbstractQueryViewModel;
import rs.ltt.android.ui.model.SearchQueryViewModel;
import rs.ltt.android.ui.model.SearchQueryViewModelFactory;


public class SearchQueryFragment extends AbstractQueryFragment {

    private SearchQueryViewModel searchQueryViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final Bundle bundle = getArguments();
        final String term = SearchQueryFragmentArgs.fromBundle(bundle == null ? new Bundle() : bundle).getText();
        this.searchQueryViewModel = ViewModelProviders.of(this, new SearchQueryViewModelFactory(getActivity().getApplication(), term)).get(SearchQueryViewModel.class);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected AbstractQueryViewModel getQueryViewModel() {
        return this.searchQueryViewModel;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.fab.setVisibility(View.GONE);
        searchQueryViewModel.getSearchTerm().observe(this, searchTerm -> {
            if (searchTerm == null) {
                return;
            }
            final Activity activity = getActivity();
            if (activity instanceof OnTermSearched) {
                ((OnTermSearched) activity).onTermSearched(searchTerm);
            }
        });
    }

    @Override
    protected QueryItemTouchHelper.Swipable onQueryItemSwipe(ThreadOverviewItem item) {
        return QueryItemTouchHelper.Swipable.NO;
    }

    @Override
    protected void onQueryItemSwiped(ThreadOverviewItem item) {

    }

    public interface OnTermSearched {
        void onTermSearched(String term);
    }

}
