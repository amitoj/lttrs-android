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

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import rs.ltt.android.entity.EntityStateEntity;
import rs.ltt.android.entity.EntityType;
import rs.ltt.android.entity.MailboxEntity;
import rs.ltt.android.entity.MailboxOverviewItem;
import rs.ltt.jmap.common.entity.Mailbox;
import rs.ltt.jmap.common.entity.Role;
import rs.ltt.jmap.mua.cache.Update;

@Dao
public abstract class MailboxDao extends AbstractEntityDao<Mailbox> {

    @Insert
    protected abstract void insert(MailboxEntity mailboxEntity);

    @Insert
    protected abstract void insert(List<MailboxEntity> mailboxEntities);

    @androidx.room.Update
    protected abstract void update(List<MailboxEntity> mailboxEntities);

    @Query("select * from mailbox where role is not null")
    public abstract List<MailboxEntity> getSpecialMailboxes();

    @Query("select id,parentId,name,sortOrder,unreadThreads,totalThreads,role from mailbox")
    public abstract LiveData<List<MailboxOverviewItem>> getMailboxes();

    @Query("select id,parentId,name,sortOrder,unreadThreads,totalThreads,role from mailbox where role=:role limit 1")
    public abstract LiveData<MailboxOverviewItem> getMailboxOverviewItem(Role role);

    @Query("select id,parentid,name,sortOrder,unreadThreads,totalThreads,role from mailbox where id=:id")
    public abstract LiveData<MailboxOverviewItem> getMailboxOverviewItem(String id);

    @Query("update mailbox set totalEmails=:value where id=:id")
    public abstract void updateTotalEmails(String id, Integer value);

    @Query("update mailbox set unreadEmails=:value where id=:id")
    public abstract void updateUnreadEmails(String id, Integer value);

    @Query("update mailbox set totalThreads=:value where id=:id")
    public abstract void updateTotalThreads(String id, Integer value);

    @Query("update mailbox set unreadThreads=:value where id=:id")
    public abstract void updateUnreadThreads(String id, Integer value);

    @Query("delete from mailbox where id=:id")
    public abstract void delete(String id);

    @Query("delete from mailbox")
    public abstract void deleteAll();

    @Transaction
    public void set(List<MailboxEntity> mailboxEntities, String state) {
        if (state != null && state.equals(getState(EntityType.MAILBOX))) {
            Log.d("lttrs","nothing to do. mailboxes with this state have already been set");
            return;
        }
        deleteAll();
        if (mailboxEntities.size() > 0) {
            insert(mailboxEntities);
        }
        insert(new EntityStateEntity(EntityType.MAILBOX, state));
    }

    @Transaction
    public void update(final Update<Mailbox> update, final String[] updatedProperties) {
        final String newState = update.getNewTypedState().getState();
        if (newState != null && newState.equals(getState(EntityType.MAILBOX))) {
            Log.d("lttrs","nothing to do. mailboxes already at newest state");
            return;
        }
        for (Mailbox mailbox : update.getCreated()) {
            insert(MailboxEntity.of(mailbox));
        }
        if (updatedProperties == null) {
            List<MailboxEntity> updatedEntities = new ArrayList<>();
            for (Mailbox mailbox : update.getUpdated()) {
                updatedEntities.add(MailboxEntity.of(mailbox));
            }
            update(updatedEntities);
        } else {
            for (Mailbox mailbox : update.getUpdated()) {
                for (String property : updatedProperties) {
                    switch (property) {
                        case "totalEmails":
                            updateTotalEmails(mailbox.getId(), mailbox.getTotalEmails());
                            break;
                        case "unreadEmails":
                            updateUnreadEmails(mailbox.getId(), mailbox.getUnreadEmails());
                            break;
                        case "totalThreads":
                            updateTotalThreads(mailbox.getId(), mailbox.getTotalThreads());
                            break;
                        case "unreadThreads":
                            updateUnreadThreads(mailbox.getId(), mailbox.getUnreadThreads());
                            break;
                        default:
                            throw new IllegalArgumentException("Unable to update property '" + property + "'");
                    }
                }
            }
        }
        for(String id : update.getDestroyed()) {
            delete(id);
        }
        throwOnUpdateConflict(EntityType.MAILBOX, update.getOldTypedState(), update.getNewTypedState());
    }
}
