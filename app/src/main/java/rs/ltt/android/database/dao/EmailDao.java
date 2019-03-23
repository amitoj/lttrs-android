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
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import rs.ltt.android.entity.EmailBodyPartEntity;
import rs.ltt.android.entity.EmailBodyValueEntity;
import rs.ltt.android.entity.EmailWithKeywords;
import rs.ltt.android.entity.EmailEmailAddressEntity;
import rs.ltt.android.entity.EmailEntity;
import rs.ltt.android.entity.EmailKeywordEntity;
import rs.ltt.android.entity.EmailMailboxEntity;
import rs.ltt.android.entity.EntityStateEntity;
import rs.ltt.android.entity.EntityType;
import rs.ltt.android.entity.FullEmail;
import rs.ltt.android.entity.ThreadHeader;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.jmap.common.entity.Email;
import rs.ltt.jmap.common.entity.TypedState;
import rs.ltt.jmap.mua.cache.Update;

@Dao
public abstract class EmailDao extends AbstractEntityDao<Email> {

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
    @Query("select id,receivedAt,preview,email.threadId from thread_item join email on thread_item.emailId=email.id where thread_item.threadId=:threadId order by position")
    public abstract DataSource.Factory<Integer, FullEmail> getEmails(String threadId);

    @Transaction
    @Query("select subject,email.threadId from thread_item join email on thread_item.emailId=email.id where thread_item.threadId=:threadId order by position limit 1")
    public abstract LiveData<ThreadHeader> getThreadHeader(String threadId);

    @Query("delete from email")
    abstract void deleteAll();

    @Transaction
    public void set(final Email[] emails, final String state) {
        if (state != null && state.equals(getState(EntityType.EMAIL))) {
            Log.d("lttrs", "nothing to do. emails with this state have already been set");
            return;
        }
        deleteAll();
        if (emails.length > 0) {
            insertEmails(emails);
        }
        insert(new EntityStateEntity(EntityType.EMAIL, state));
    }

    @Query("delete from keyword_overwrite where threadId=(select threadId from email where id=:emailId)")
    protected abstract void deleteKeywordToggle(String emailId);

    @Transaction
    public void add(final TypedState<Email> expectedState, Email[] email) {
        if (email.length > 0) {
            insertEmails(email);
        }
        throwOnCacheConflict(EntityType.EMAIL, expectedState);
    }

    @Query("SELECT EXISTS(SELECT 1 FROM email WHERE id=:emailId)")
    protected abstract boolean exists(String emailId);

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
                if (!exists(email.getId())) {
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

            }
        }
        for (String id : update.getDestroyed()) {
            deleteEmail(id);
        }
        throwOnUpdateConflict(EntityType.EMAIL, update.getOldTypedState(), update.getNewTypedState());
    }
}
