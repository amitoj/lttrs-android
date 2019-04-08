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

    final QueryRepository queryRepository;

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

    public void toggleFlagged(String threadId, boolean target) {
        this.queryRepository.toggleFlagged(threadId, target);
    }
}
