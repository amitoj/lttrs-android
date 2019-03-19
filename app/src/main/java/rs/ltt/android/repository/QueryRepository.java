package rs.ltt.android.repository;

import android.app.Application;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import rs.ltt.android.Credentials;
import rs.ltt.android.cache.DatabaseCache;
import rs.ltt.android.database.LttrsDatabase;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.jmap.client.session.SessionFileCache;
import rs.ltt.jmap.common.entity.EmailQuery;
import rs.ltt.jmap.mua.Mua;

public class QueryRepository {

    private final LttrsDatabase database;

    private final Mua mua;

    private final Set<String> runningQueries = new HashSet<>();
    private final Set<String> runningPagingRequests = new HashSet<>();

    private MutableLiveData<Set<String>> runningQueriesLiveData = new MutableLiveData<>(runningQueries);
    private MutableLiveData<Set<String>> runningPagingRequestsLiveData = new MutableLiveData<>(runningPagingRequests);


    public QueryRepository(Application application) {
        this.database = LttrsDatabase.getInstance(application, Credentials.username);
        this.mua = Mua.builder()
                .password(Credentials.password)
                .username(Credentials.username)
                .cache(new DatabaseCache(this.database))
                .sessionCache(new SessionFileCache(application.getCacheDir()))
                .queryPageSize(20)
                .build();
    }

    public LiveData<PagedList<ThreadOverviewItem>> getThreadOverviewItems(final EmailQuery query) {
        return new LivePagedListBuilder<>(database.queryDao().getThreadOverviewItems(query.toQueryString()), 30)
                .setBoundaryCallback(new PagedList.BoundaryCallback<ThreadOverviewItem>() {
                    @Override
                    public void onZeroItemsLoaded() {
                        refresh(query); //conceptually in terms of loading indicators this is more of a page request
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


    public LiveData<Boolean> isRunningQueryFor(final EmailQuery query) {
        return Transformations.map(runningQueriesLiveData, queryStrings -> queryStrings.contains(query.toQueryString()));

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
        }
        ListenableFuture<Boolean> hadResults = mua.query(emailQuery, afterEmailId);
        hadResults.addListener(() -> {
            final boolean modifiedImpliciedRefresh;
            synchronized (this) {
                runningPagingRequests.remove(queryString);
                modifiedImpliciedRefresh = runningQueries.remove(queryString);
            }
            runningPagingRequestsLiveData.postValue(runningPagingRequests);
            if (modifiedImpliciedRefresh) {
                runningQueriesLiveData.postValue(runningQueries);
            }
            try {
                Log.d("lttrs", "had items=" + hadResults.get());
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                Log.d("lttrs", "error retrieving the next page", cause);
            } catch (Exception e) {
                Log.d("lttrs", "error paging ", e);
            }
        }, MoreExecutors.directExecutor());
    }

}
