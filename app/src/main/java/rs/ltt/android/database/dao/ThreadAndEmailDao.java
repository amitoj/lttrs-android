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

package rs.ltt.android.database.dao;

import android.util.Log;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import rs.ltt.android.entity.EmailBodyPartEntity;
import rs.ltt.android.entity.EmailBodyValueEntity;
import rs.ltt.android.entity.EmailEmailAddressEntity;
import rs.ltt.android.entity.EmailEntity;
import rs.ltt.android.entity.EmailKeywordEntity;
import rs.ltt.android.entity.EmailMailboxEntity;
import rs.ltt.android.entity.EmailWithKeywords;
import rs.ltt.android.entity.EmailWithMailboxes;
import rs.ltt.android.entity.EntityStateEntity;
import rs.ltt.android.entity.EntityType;
import rs.ltt.android.entity.FullEmail;
import rs.ltt.android.entity.ThreadEntity;
import rs.ltt.android.entity.ThreadHeader;
import rs.ltt.android.entity.ThreadItemEntity;
import rs.ltt.jmap.common.entity.Email;
import rs.ltt.jmap.common.entity.Thread;
import rs.ltt.jmap.common.entity.TypedState;
import rs.ltt.jmap.mua.cache.CacheConflictException;
import rs.ltt.jmap.mua.cache.Missing;
import rs.ltt.jmap.mua.cache.Update;

@Dao
public abstract class ThreadAndEmailDao extends AbstractEntityDao {

    @Insert
    abstract void insert(ThreadEntity entity);

    @Insert
    abstract void insert(List<ThreadItemEntity> entities);

    @Query("delete from thread_item where threadId=:threadId")
    abstract void deleteAllThreadItem(String threadId);

    @Delete
    abstract void delete(ThreadEntity thread);

    @Query("delete from thread")
    abstract void deleteAllThread();

    private void set(Thread[] threads, String state) {
        if (state != null && state.equals(getState(EntityType.THREAD))) {
            Log.d("lttrs","nothing to do. threads with this state have already been set");
            return;
        }
        deleteAllThread();
        if (threads.length > 0) {
            insertThreads(threads);
        }
        insert(new EntityStateEntity(EntityType.THREAD, state));
    }

    private void add(final TypedState<Thread> expectedState, Thread[] threads) {
        if (threads.length > 0) {
            insertThreads(threads);
        }
        throwOnCacheConflict(EntityType.THREAD, expectedState);
    }

    private void insertThreads(Thread[] threads) {
        for (Thread thread : threads) {
            insert(ThreadEntity.of(thread));
            insert(ThreadItemEntity.of(thread));
        }
    }

    @Query("SELECT EXISTS(SELECT 1 FROM thread WHERE threadId=:threadId)")
    protected abstract boolean threadExists(String threadId);

    @Transaction
    public void update(Update<Thread> update) {
        final String newState = update.getNewTypedState().getState();
        if (newState != null && newState.equals(getState(EntityType.THREAD))) {
            Log.d("lttrs","nothing to do. threads already at newest state");
            return;
        }
        Thread[] created = update.getCreated();
        if (created.length > 0) {
            insertThreads(created);
        }
        for (Thread thread : update.getUpdated()) {
            if (threadExists(thread.getId())) {
                deleteAllThreadItem(thread.getId());
                insert(ThreadItemEntity.of(thread));
            } else {
                Log.d("lttrs","skipping update to thread "+thread.getId());
            }
        }
        for(String id : update.getDestroyed()) {
            delete(ThreadEntity.of(id));
        }
        throwOnUpdateConflict(EntityType.THREAD, update.getOldTypedState(), update.getNewTypedState());
    }

    @Query(" select threadId from `query` join query_item on `query`.id = queryId where threadId not in(select thread.threadId from thread) and queryString=:queryString")
    public abstract List<String> getMissingThreadIds(String queryString);

    @Transaction
    public Missing getMissing(String queryString) {
        final List<String> ids = getMissingThreadIds(queryString);
        final String threadState = getState(EntityType.THREAD);
        final String emailState = getState(EntityType.EMAIL);
        return new Missing(threadState, emailState, ids);
    }

    @Query("delete from email where id=:id")
    abstract void deleteEmail(String id);

    @Query("delete from email_keyword where emailId=:emailId")
    abstract void deleteKeywords(String emailId);

    @Query("delete from email_mailbox where emailId=:emailId")
    abstract void deleteMailboxes(String emailId);

    @Insert
    abstract void insert(EmailEntity entity);

    @Insert
    abstract void insertEmailAddresses(List<EmailEmailAddressEntity> entities);

    @Insert
    abstract void insertMailboxes(List<EmailMailboxEntity> entities);

    @Insert
    abstract void insertKeywords(List<EmailKeywordEntity> entities);

    @Insert
    abstract void insertEmailBodyValues(List<EmailBodyValueEntity> entities);

    @Insert
    abstract void insertEmailBodyParts(List<EmailBodyPartEntity> entities);

    @Transaction
    @Query("select id from email where threadId=:threadId")
    public abstract List<EmailWithKeywords> getEmailsWithKeywords(String threadId);

    @Transaction
    @Query("select id from email where threadId=:threadId")
    public abstract List<EmailWithMailboxes> getEmailsWithMailboxes(String threadId);

    @Transaction
    @Query("select id,receivedAt,preview,email.threadId from thread_item join email on thread_item.emailId=email.id where thread_item.threadId=:threadId order by position")
    public abstract DataSource.Factory<Integer, FullEmail> getEmails(String threadId);

    @Transaction
    @Query("select subject,email.threadId from thread_item join email on thread_item.emailId=email.id where thread_item.threadId=:threadId order by position limit 1")
    public abstract LiveData<ThreadHeader> getThreadHeader(String threadId);

    @Query("delete from email")
    abstract void deleteAllEmail();

    private void set(final Email[] emails, final String state) {
        if (state != null && state.equals(getState(EntityType.EMAIL))) {
            Log.d("lttrs", "nothing to do. emails with this state have already been set");
            return;
        }
        deleteAllEmail();
        if (emails.length > 0) {
            insertEmails(emails);
        }
        insert(new EntityStateEntity(EntityType.EMAIL, state));
    }

    @Query("delete from keyword_overwrite where threadId=(select threadId from email where id=:emailId)")
    protected abstract void deleteKeywordToggle(String emailId);

    @Query("delete from mailbox_overwrite where threadId=(select threadId from email where id=:emailId)")
    protected abstract void deleteMailboxOverwrite(String emailId);

    @Query("update query_item_overwrite set executed=1 where threadId IN(select email.threadid from email where email.id=:emailId)")
    protected abstract int markAsExecuted(String emailId);

    @Transaction
    public void add(final TypedState<Thread> expectedThreadState, Thread[] threads, final TypedState<Email> expectedEmailState, final Email[] emails) {
        add(expectedThreadState, threads);
        add(expectedEmailState, emails);
    }

    @Transaction
    public void set(final TypedState<Thread> threadState, Thread[] threads, final TypedState<Email> emailState, final Email[] emails) {
        set(threads, threadState.getState());
        set(emails, emailState.getState());
    }

    private void add(final TypedState<Email> expectedState, Email[] email) {
        if (email.length > 0) {
            insertEmails(email);
        }
        throwOnCacheConflict(EntityType.EMAIL, expectedState);
    }

    @Query("SELECT EXISTS(SELECT 1 FROM email WHERE id=:emailId)")
    protected abstract boolean emailExists(String emailId);

    private void insertEmails(final Email[] emails) {
        for (Email email : emails) {
            insert(EmailEntity.of(email));
            insertEmailAddresses(EmailEmailAddressEntity.of(email));
            insertMailboxes(EmailMailboxEntity.of(email));
            insertKeywords(EmailKeywordEntity.of(email));
            insertEmailBodyParts(EmailBodyPartEntity.of(email));
            insertEmailBodyValues(EmailBodyValueEntity.of(email));
        }
    }

    @Transaction
    public void updateEmails(Update<Email> update, String[] updatedProperties) {
        final String newState = update.getNewTypedState().getState();
        if (newState != null && newState.equals(getState(EntityType.EMAIL))) {
            Log.d("lttrs", "nothing to do. emails already at newest state");
            return;
        }
        final Email[] created = update.getCreated();
        if (created.length > 0) {
            insertEmails(created);
        }
        if (updatedProperties != null) {
            for (Email email : update.getUpdated()) {
                if (!emailExists(email.getId())) {
                    Log.d("lttrs", "skipping updates to email " + email.getId() + " because we don’t have that");
                    continue;
                }
                for (String property : updatedProperties) {
                    switch (property) {
                        case "keywords":
                            deleteKeywords(email.getId());
                            insertKeywords(EmailKeywordEntity.of(email));
                            break;
                        case "mailboxIds":
                            deleteMailboxes(email.getId());
                            insertMailboxes(EmailMailboxEntity.of(email));
                            break;
                        default:
                            throw new IllegalArgumentException("Unable to update property '" + property + "'");
                    }
                }

                deleteKeywordToggle(email.getId());
                deleteMailboxOverwrite(email.getId());
                int count = markAsExecuted(email.getId());
                Log.d("lttrs","marked "+count+" as executed");

            }
        }
        for (String id : update.getDestroyed()) {
            deleteEmail(id);
        }
        throwOnUpdateConflict(EntityType.EMAIL, update.getOldTypedState(), update.getNewTypedState());
    }
}
