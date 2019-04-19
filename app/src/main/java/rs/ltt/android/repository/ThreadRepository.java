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

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import rs.ltt.android.entity.ExpandedPosition;
import rs.ltt.android.entity.FullEmail;
import rs.ltt.android.entity.KeywordOverwriteEntity;
import rs.ltt.android.entity.MailboxOverwriteEntity;
import rs.ltt.android.entity.MailboxWithRoleAndName;
import rs.ltt.android.entity.ThreadHeader;

public class ThreadRepository extends LttrsRepository {

    public ThreadRepository(Application application) {
        super(application);
    }

    public LiveData<PagedList<FullEmail>> getEmails(String threadId) {
        return new LivePagedListBuilder<>(database.threadAndEmailDao().getEmails(threadId), 30).build();
    }

    public LiveData<ThreadHeader> getThreadHeader(String threadId) {
        return database.threadAndEmailDao().getThreadHeader(threadId);
    }

    public LiveData<List<MailboxWithRoleAndName>> getMailboxes(String threadId) {
        return database.mailboxDao().getMailboxesForThreadLiveData(threadId);
    }


    public LiveData<List<MailboxOverwriteEntity>> getMailboxOverwrites(String threadId) {
        return database.overwriteDao().getMailboxOverwrites(threadId);
    }

    public ListenableFuture<List<ExpandedPosition>> getExpandedPositions(String threadId) {
        ListenableFuture<KeywordOverwriteEntity> overwriteFuture = database.overwriteDao().getKeywordOverwrite(threadId);
        return Futures.transformAsync(overwriteFuture, input -> {
            if (input != null) {
                if (input.value) {
                    return database.threadAndEmailDao().getMaxPosition(threadId);
                } else {
                    return database.threadAndEmailDao().getAllPositions(threadId);
                }
            } else {
                ListenableFuture<List<ExpandedPosition>> unseen = database.threadAndEmailDao().getUnseenPositions(threadId);
                return Futures.transformAsync(unseen, input1 -> {
                    if (input1 == null || input1.size() == 0) {
                        return database.threadAndEmailDao().getMaxPosition(threadId);
                    } else {
                        return Futures.immediateFuture(input1);
                    }
                }, MoreExecutors.directExecutor());
            }
        }, MoreExecutors.directExecutor());
    }
}
