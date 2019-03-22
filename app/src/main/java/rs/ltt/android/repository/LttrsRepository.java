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

public abstract class LttrsRepository {

    protected final LttrsDatabase database;

    protected final Mua mua;

    protected final Executor ioExecutor = Executors.newSingleThreadExecutor();


    public LttrsRepository(Application application) {
        this.database = LttrsDatabase.getInstance(application, Credentials.username);
        this.mua = Mua.builder()
                .password(Credentials.password)
                .username(Credentials.username)
                .cache(new DatabaseCache(this.database))
                .sessionCache(new SessionFileCache(application.getCacheDir()))
                .queryPageSize(20)
                .build();
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
