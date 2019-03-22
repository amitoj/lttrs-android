package rs.ltt.android.repository;

import android.app.Application;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import rs.ltt.android.Credentials;
import rs.ltt.android.cache.DatabaseCache;
import rs.ltt.android.database.LttrsDatabase;
import rs.ltt.android.entity.KeywordOverwriteEntity;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.android.worker.ModifyKeywordWorker;
import rs.ltt.jmap.client.session.SessionFileCache;
import rs.ltt.jmap.common.entity.EmailQuery;
import rs.ltt.jmap.common.entity.Keyword;
import rs.ltt.jmap.mua.Mua;

public class QueryRepository extends LttrsRepository {


    private final Set<String> runningQueries = new HashSet<>();
    private final Set<String> runningPagingRequests = new HashSet<>();

    private MutableLiveData<Set<String>> runningQueriesLiveData = new MutableLiveData<>(runningQueries);
    private MutableLiveData<Set<String>> runningPagingRequestsLiveData = new MutableLiveData<>(runningPagingRequests);

    private final Executor ioExecutor = Executors.newSingleThreadExecutor();


    public QueryRepository(Application application) {
        super(application);
    }

    public LiveData<PagedList<ThreadOverviewItem>> getThreadOverviewItems(final EmailQuery query) {
        return new LivePagedListBuilder<>(database.queryDao().getThreadOverviewItems(query.toQueryString()), 30)
                .setBoundaryCallback(new PagedList.BoundaryCallback<ThreadOverviewItem>() {
                    @Override
                    public void onZeroItemsLoaded() {
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

    private void insert(KeywordOverwriteEntity keywordOverwriteEntity) {
        Log.d("lttrs","db insert keyword overwrite "+keywordOverwriteEntity.value);
        ioExecutor.execute(() -> database.keywordToggleDao().insert(keywordOverwriteEntity));
    }

    public void toggleFlagged(final String threadId, final boolean targetState) {
        final KeywordOverwriteEntity keywordOverwriteEntity = new KeywordOverwriteEntity(threadId, Keyword.FLAGGED, targetState);
        insert(keywordOverwriteEntity);

        final Data inputData = new Data.Builder()
                .putString("threadId", threadId)
                .putString("keyword", Keyword.FLAGGED)
                .putBoolean("target", targetState)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();


        final String uniqueWorkName = "toggle-keyword-" + Keyword.FLAGGED + "-" + threadId;

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ModifyKeywordWorker.class)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build();
        WorkManager workManager = WorkManager.getInstance();
        workManager.enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, workRequest);
    }
}
