package rs.ltt.android.ui.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.android.repository.QueryRepository;
import rs.ltt.jmap.common.entity.EmailQuery;

public abstract class AbstractQueryViewModel extends AndroidViewModel {

    private final QueryRepository queryRepository;

    private LiveData<PagedList<ThreadOverviewItem>> threads;

    private LiveData<Boolean> refreshing;

    AbstractQueryViewModel(@NonNull Application application) {
        super(application);
        this.queryRepository = new QueryRepository(application);
    }

    void init() {
        this.threads = Transformations.switchMap(getQuery(), queryRepository::getThreadOverviewItems);
        this.refreshing = Transformations.switchMap(getQuery(), queryRepository::isRunningQueryFor);
    }

    public LiveData<Boolean> isRefreshing() {
        final LiveData<Boolean> refreshing = this.refreshing;
        if (refreshing == null) {
            throw new IllegalStateException("LiveData for refreshing not initialized. Forgot to call init()?");
        }
        return refreshing;
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
