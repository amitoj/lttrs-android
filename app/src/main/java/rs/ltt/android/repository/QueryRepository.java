package rs.ltt.android.repository;

import android.app.Application;
import android.util.Log;

import com.google.common.util.concurrent.MoreExecutors;

import java.util.HashSet;
import java.util.Set;

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
import rs.ltt.jmap.common.entity.EmailQuery;
import rs.ltt.jmap.mua.Mua;

public class QueryRepository {

    private final LttrsDatabase database;

    private final DatabaseCache muaCache;

    private final Set<String> runningQueries = new HashSet<>();

    private MutableLiveData<Set<String>> runningQueriesLiveData = new MutableLiveData<>(runningQueries);


    public QueryRepository(Application application) {
        this.database = LttrsDatabase.getInstance(application, Credentials.username);
        this.muaCache = new DatabaseCache(this.database);
    }

    public LiveData<PagedList<ThreadOverviewItem>> getThreadOverviewItems(final EmailQuery query) {
        return new LivePagedListBuilder<>(database.queryDao().getThreadOverviewItems(query.toQueryString()), 30)
                .setBoundaryCallback(new PagedList.BoundaryCallback<ThreadOverviewItem>() {
                    @Override
                    public void onZeroItemsLoaded() {
                        refresh(query);
                        super.onZeroItemsLoaded();
                    }

                    @Override
                    public void onItemAtEndLoaded(@NonNull ThreadOverviewItem itemAtEnd) {
                        Log.d("lttrs","onItemAtEndLoaded("+itemAtEnd.threadId+")");
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
        synchronized (runningQueries) {
            if (!runningQueries.add(queryString)) {
                Log.d("lttrs","skipping refresh since already running");
                return;
            }

        }
        Mua mua = Mua.builder()
                .password(Credentials.password)
                .username(Credentials.username)
                .cache(this.muaCache)
                .build();
        mua.query(emailQuery).addListener(() -> {
            synchronized (runningQueries) {
                runningQueries.remove(queryString);
            }
            runningQueriesLiveData.postValue(runningQueries);
        }, MoreExecutors.directExecutor());
    }

}
