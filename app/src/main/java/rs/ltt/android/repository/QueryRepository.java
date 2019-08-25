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

package rs.ltt.android.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import rs.ltt.android.entity.MailboxWithRoleAndName;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.jmap.common.entity.Role;
import rs.ltt.jmap.common.entity.query.EmailQuery;

public class QueryRepository extends LttrsRepository {


    private final Set<String> runningQueries = new HashSet<>();
    private final Set<String> runningPagingRequests = new HashSet<>();

    private MutableLiveData<Set<String>> runningQueriesLiveData = new MutableLiveData<>(runningQueries);
    private MutableLiveData<Set<String>> runningPagingRequestsLiveData = new MutableLiveData<>(runningPagingRequests);


    public QueryRepository(Application application) {
        super(application);
    }

    public LiveData<PagedList<ThreadOverviewItem>> getThreadOverviewItems(final EmailQuery query) {
        return new LivePagedListBuilder<>(database.queryDao().getThreadOverviewItems(query.toQueryString()), 30)
                .setBoundaryCallback(new PagedList.BoundaryCallback<ThreadOverviewItem>() {
                    @Override
                    public void onZeroItemsLoaded() {
                        Log.d("lttrs","onZeroItemsLoaded");
                        requestNextPage(query, null); //conceptually in terms of loading indicators this is more of a page request
                        super.onZeroItemsLoaded();
                    }

                    @Override
                    public void onItemAtEndLoaded(@NonNull ThreadOverviewItem itemAtEnd) {
                        Log.d("lttrs", "onItemAtEndLoaded(" + itemAtEnd.emailId + ")");
                        requestNextPage(query, itemAtEnd.emailId);
                        super.onItemAtEndLoaded(itemAtEnd);
                    }
                })
                .build();
    }

    public ListenableFuture<MailboxWithRoleAndName> getInbox() {
        return database.mailboxDao().getMailbox(Role.INBOX);
    }


    public LiveData<Boolean> isRunningQueryFor(final EmailQuery query) {
        return Transformations.map(runningQueriesLiveData, queryStrings -> queryStrings.contains(query.toQueryString()));
    }

    public LiveData<Boolean> isRunningPagingRequestFor(final EmailQuery query) {
        return Transformations.map(runningPagingRequestsLiveData, queryStrings -> queryStrings.contains(query.toQueryString()));
    }

    public void refresh(final EmailQuery emailQuery) {
        final String queryString = emailQuery.toQueryString();
        synchronized (this) {
            if (!runningQueries.add(queryString)) {
                Log.d("lttrs", "skipping refresh since already running");
                return;
            }
            if (runningPagingRequests.contains(queryString)) {
                //even though this refresh call is only implicit through the pageRequest we want to display something nice for the user
                runningQueries.add(queryString);
                runningQueriesLiveData.postValue(runningQueries);
                Log.d("lttrs", "skipping refresh since we are running a page request");
                return;
            }

        }
        mua.query(emailQuery).addListener(() -> {
            synchronized (runningQueries) {
                runningQueries.remove(queryString);
            }
            runningQueriesLiveData.postValue(runningQueries);
        }, MoreExecutors.directExecutor());
    }


    public void requestNextPage(final EmailQuery emailQuery, String afterEmailId) {
        final String queryString = emailQuery.toQueryString();
        synchronized (this) {
            if (!runningPagingRequests.add(queryString)) {
                Log.d("lttrs", "skipping paging request since already running");
                return;
            }
            runningPagingRequestsLiveData.postValue(runningPagingRequests);
        }
        final ListenableFuture hadResults;
        if (afterEmailId == null) {
            hadResults = mua.query(emailQuery);
        } else {
            hadResults = mua.query(emailQuery, afterEmailId);
        }
        hadResults.addListener(() -> {
            final boolean modifiedImplicitRefresh;
            synchronized (this) {
                runningPagingRequests.remove(queryString);
                modifiedImplicitRefresh = runningQueries.remove(queryString);
            }
            runningPagingRequestsLiveData.postValue(runningPagingRequests);
            if (modifiedImplicitRefresh) {
                runningQueriesLiveData.postValue(runningQueries);
            }
            try {
                Log.d("lttrs", "requestNextPageResult=" + hadResults.get());
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                Log.d("lttrs", "error retrieving the next page", cause);
            } catch (Exception e) {
                Log.d("lttrs", "error paging ", e);
            }
        }, MoreExecutors.directExecutor());
    }
}
