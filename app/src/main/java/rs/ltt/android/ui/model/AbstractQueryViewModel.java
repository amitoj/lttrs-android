package rs.ltt.android.ui.model;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import rs.ltt.android.entity.KeywordOverwriteEntity;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.android.repository.QueryRepository;
import rs.ltt.android.worker.ModifyKeywordWorker;
import rs.ltt.jmap.common.entity.EmailQuery;
import rs.ltt.jmap.common.entity.Keyword;

public abstract class AbstractQueryViewModel extends AndroidViewModel {

    private final QueryRepository queryRepository;

    private LiveData<PagedList<ThreadOverviewItem>> threads;

    private LiveData<Boolean> refreshing;

    private LiveData<Boolean> runningPagingRequest;

    AbstractQueryViewModel(@NonNull Application application) {
        super(application);
        this.queryRepository = new QueryRepository(application);
    }

    void init() {
        this.threads = Transformations.switchMap(getQuery(), queryRepository::getThreadOverviewItems);
        this.refreshing = Transformations.switchMap(getQuery(), queryRepository::isRunningQueryFor);
        this.runningPagingRequest = Transformations.switchMap(getQuery(), queryRepository::isRunningPagingRequestFor);
    }

    public LiveData<Boolean> isRefreshing() {
        final LiveData<Boolean> refreshing = this.refreshing;
        if (refreshing == null) {
            throw new IllegalStateException("LiveData for refreshing not initialized. Forgot to call init()?");
        }
        return refreshing;
    }

    public LiveData<Boolean> isRunningPagingRequest() {
        final LiveData<Boolean> paging = this.runningPagingRequest;
        if (paging == null) {
            throw new IllegalStateException("LiveData for paging not initialized. Forgot to call init()?");
        }
        return paging;
    }

    public void toggleFlagged(ThreadOverviewItem item) {
        final boolean targetState = !item.showAsFlagged();
        final KeywordOverwriteEntity keywordOverwriteEntity = new KeywordOverwriteEntity(item.threadId, Keyword.FLAGGED, targetState);
        queryRepository.insert(keywordOverwriteEntity);

        final Data inputData = new Data.Builder()
                .putString("threadId", item.threadId)
                .putString("keyword", Keyword.FLAGGED)
                .putBoolean("target", targetState)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();


        final String uniqueWorkName = "toggle-keyword-" + Keyword.FLAGGED + "-" + item.threadId;

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ModifyKeywordWorker.class)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build();
        WorkManager workManager = WorkManager.getInstance();
        workManager.enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public LiveData<PagedList<ThreadOverviewItem>> getThreadOverviewItems() {
        final LiveData<PagedList<ThreadOverviewItem>> liveData = this.threads;
        if (liveData == null) {
            throw new IllegalStateException("LiveData for thread items not initialized. Forgot to call init()?");
        }
        return liveData;
    }

    public void onRefresh() {
        final EmailQuery emailQuery = getQuery().getValue();
        if (emailQuery != null) {
            queryRepository.refresh(emailQuery);
        }
    }

    protected abstract LiveData<EmailQuery> getQuery();
}
