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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
import rs.ltt.android.entity.MailboxOverviewItem;
import rs.ltt.android.entity.MailboxOverwriteEntity;
import rs.ltt.android.entity.MailboxWithRoleAndName;
import rs.ltt.android.entity.QueryEntity;
import rs.ltt.android.entity.QueryItemOverwriteEntity;
import rs.ltt.android.worker.ArchiveWorker;
import rs.ltt.android.worker.ModifyKeywordWorker;
import rs.ltt.android.worker.MoveToInboxWorker;
import rs.ltt.android.worker.MoveToTrashWorker;
import rs.ltt.android.worker.MuaWorker;
import rs.ltt.android.worker.RemoveFromMailboxWorker;
import rs.ltt.jmap.client.session.SessionFileCache;
import rs.ltt.jmap.common.entity.EmailQuery;
import rs.ltt.jmap.common.entity.IdentifiableMailboxWithRole;
import rs.ltt.jmap.common.entity.Keyword;
import rs.ltt.jmap.common.entity.Role;
import rs.ltt.jmap.common.entity.filter.EmailFilterCondition;
import rs.ltt.jmap.mua.Mua;

public abstract class LttrsRepository {

    private static final Constraints CONNECTED_CONSTRAINT = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

    protected final LttrsDatabase database;

    protected final Mua mua;

    private final Executor ioExecutor = Executors.newSingleThreadExecutor();


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

    private void insert(final KeywordOverwriteEntity keywordOverwriteEntity) {
        Log.d("lttrs", "db insert keyword overwrite " + keywordOverwriteEntity.value);
        database.overwriteDao().insert(keywordOverwriteEntity);
    }

    private void insertQueryItemOverwrite(final String threadId, final Role role) {
        MailboxOverviewItem mailbox = database.mailboxDao().getMailboxOverviewItem(role);
        if (mailbox != null) {
            insertQueryItemOverwrite(threadId, mailbox);
        }
    }

    private void insertQueryItemOverwrite(final String threadId, final IdentifiableMailboxWithRole mailbox) {
        final String queryString = EmailQuery.of(EmailFilterCondition.builder().inMailbox(mailbox.getId()).build(), true).toQueryString();
        QueryEntity queryEntity = database.queryDao().get(queryString);
        if (queryEntity != null) {
            database.overwriteDao().insert(new QueryItemOverwriteEntity(queryEntity.id, threadId));
        } else {
            Log.d("lttrs", "do not enter overwrite");
        }
    }

    private void deleteQueryItemOverwrite(final String threadId, final Role role) {
        MailboxOverviewItem mailbox = database.mailboxDao().getMailboxOverviewItem(role);
        if (mailbox != null) {
            deleteQueryItemOverwrite(threadId, mailbox);
        }
    }

    private void deleteQueryItemOverwrite(final String threadId, final IdentifiableMailboxWithRole mailbox) {
        final String queryString = EmailQuery.of(EmailFilterCondition.builder().inMailbox(mailbox.getId()).build(), true).toQueryString();
        QueryEntity queryEntity = database.queryDao().get(queryString);
        if (queryEntity != null) {
            database.overwriteDao().delete(new QueryItemOverwriteEntity(queryEntity.id, threadId));
        }
    }

    public void removeFromMailbox(final String threadId, final IdentifiableMailboxWithRole mailbox) {
        ioExecutor.execute(() -> {
            insertQueryItemOverwrite(threadId, mailbox);
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(RemoveFromMailboxWorker.class)
                    .setConstraints(CONNECTED_CONSTRAINT)
                    .setInputData(RemoveFromMailboxWorker.data(threadId, mailbox))
                    .build();
            WorkManager workManager = WorkManager.getInstance();
            workManager.enqueueUniqueWork(MuaWorker.SYNC_MAILBOXES, ExistingWorkPolicy.APPEND, workRequest);
        });
    }

    public void archive(final String threadId) {
        ioExecutor.execute(() -> {
            insertQueryItemOverwrite(threadId, Role.INBOX);
            deleteQueryItemOverwrite(threadId, Role.ARCHIVE);
            database.overwriteDao().insert(MailboxOverwriteEntity.of(threadId, Role.INBOX, false));
            database.overwriteDao().insert(MailboxOverwriteEntity.of(threadId, Role.ARCHIVE, true));
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ArchiveWorker.class)
                    .setConstraints(CONNECTED_CONSTRAINT)
                    .setInputData(ArchiveWorker.data(threadId))
                    .build();
            WorkManager workManager = WorkManager.getInstance();
            workManager.enqueueUniqueWork(MuaWorker.SYNC_MAILBOXES, ExistingWorkPolicy.APPEND, workRequest);
        });
    }

    public void moveToInbox(final String threadId) {
        ioExecutor.execute(() -> {
            insertQueryItemOverwrite(threadId, Role.ARCHIVE);
            insertQueryItemOverwrite(threadId, Role.TRASH);
            deleteQueryItemOverwrite(threadId, Role.INBOX);

            database.overwriteDao().insert(MailboxOverwriteEntity.of(threadId, Role.INBOX, true));
            database.overwriteDao().insert(MailboxOverwriteEntity.of(threadId, Role.ARCHIVE, false));
            database.overwriteDao().insert(MailboxOverwriteEntity.of(threadId, Role.TRASH, false));

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MoveToInboxWorker.class)
                    .setConstraints(CONNECTED_CONSTRAINT)
                    .setInputData(MoveToInboxWorker.data(threadId))
                    .build();
            WorkManager workManager = WorkManager.getInstance();
            workManager.enqueueUniqueWork(MuaWorker.SYNC_MAILBOXES, ExistingWorkPolicy.APPEND, workRequest);
        });
    }

    public void moveToTrash(final String threadId) {
        ioExecutor.execute(() -> {
            for (MailboxWithRoleAndName mailbox : database.mailboxDao().getMailboxesForThread(threadId)) {
                if (mailbox.role != Role.TRASH) {
                    insertQueryItemOverwrite(threadId, mailbox);
                }
            }
            database.overwriteDao().insert(MailboxOverwriteEntity.of(threadId, Role.INBOX, false));
            database.overwriteDao().insert(MailboxOverwriteEntity.of(threadId, Role.TRASH, true));
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MoveToTrashWorker.class)
                    .setConstraints(CONNECTED_CONSTRAINT)
                    .setInputData(MoveToTrashWorker.data(threadId))
                    .build();
            WorkManager workManager = WorkManager.getInstance();
            workManager.enqueueUniqueWork(MuaWorker.SYNC_MAILBOXES, ExistingWorkPolicy.APPEND, workRequest);
        });
    }

    public void toggleFlagged(final String threadId, final boolean targetState) {
        toggleKeyword(threadId, Keyword.FLAGGED, targetState);
    }

    private void toggleKeyword(final String threadId, final String keyword, final boolean targetState) {
        ioExecutor.execute(() -> {
            final KeywordOverwriteEntity keywordOverwriteEntity = new KeywordOverwriteEntity(threadId, keyword, targetState);
            insert(keywordOverwriteEntity);
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ModifyKeywordWorker.class)
                    .setConstraints(CONNECTED_CONSTRAINT)
                    .setInputData(ModifyKeywordWorker.data(threadId, keyword, targetState))
                    .build();
            WorkManager workManager = WorkManager.getInstance();
            workManager.enqueueUniqueWork(ModifyKeywordWorker.uniqueName(threadId, keyword), ExistingWorkPolicy.REPLACE, workRequest);
        });
    }
}
