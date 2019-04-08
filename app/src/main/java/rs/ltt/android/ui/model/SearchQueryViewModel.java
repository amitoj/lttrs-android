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

package rs.ltt.android.ui.model;


import android.app.Application;
import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import rs.ltt.android.Credentials;
import rs.ltt.android.database.LttrsDatabase;
import rs.ltt.android.entity.MailboxOverviewItem;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.jmap.common.entity.EmailQuery;
import rs.ltt.jmap.common.entity.Role;
import rs.ltt.jmap.common.entity.filter.EmailFilterCondition;

public class SearchQueryViewModel extends AbstractQueryViewModel {

    private final LiveData<String> searchTerm;
    private final LiveData<EmailQuery> searchQueryLiveData;

    SearchQueryViewModel(final Application application, final String searchTerm) {
        super(application);
        this.searchTerm = new MutableLiveData<>(searchTerm);
        this.searchQueryLiveData = Transformations.map(this.searchTerm, text -> EmailQuery.of(EmailFilterCondition.builder().text(text).build(),true));
        init();
    }

    public LiveData<String> getSearchTerm() {
        return this.searchTerm;
    }

    //TODO: should probably be moved to AbstractQueryViewModel
    public void archive(ThreadOverviewItem item) {
        queryRepository.archive(item.threadId);
    }

    @Override
    protected LiveData<EmailQuery> getQuery() {
        return searchQueryLiveData;
    }
}
