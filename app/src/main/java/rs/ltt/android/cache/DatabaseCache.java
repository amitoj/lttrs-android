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

package rs.ltt.android.cache;

import android.util.Log;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rs.ltt.android.database.LttrsDatabase;
import rs.ltt.android.entity.EntityType;
import rs.ltt.android.entity.MailboxEntity;
import rs.ltt.jmap.common.entity.Email;
import rs.ltt.jmap.common.entity.IdentifiableMailboxWithRole;
import rs.ltt.jmap.common.entity.Identity;
import rs.ltt.jmap.common.entity.Mailbox;
import rs.ltt.jmap.common.entity.Thread;
import rs.ltt.jmap.common.entity.TypedState;
import rs.ltt.jmap.mua.cache.Cache;
import rs.ltt.jmap.mua.cache.CacheConflictException;
import rs.ltt.jmap.mua.cache.CacheReadException;
import rs.ltt.jmap.mua.cache.CacheWriteException;
import rs.ltt.jmap.mua.cache.Missing;
import rs.ltt.jmap.mua.cache.NotSynchronizedException;
import rs.ltt.jmap.mua.cache.ObjectsState;
import rs.ltt.jmap.mua.cache.QueryStateWrapper;
import rs.ltt.jmap.mua.cache.QueryUpdate;
import rs.ltt.jmap.mua.cache.Update;
import rs.ltt.jmap.mua.util.QueryResult;
import rs.ltt.jmap.mua.util.QueryResultItem;

public class DatabaseCache implements Cache {


    private final LttrsDatabase database;

    public DatabaseCache(LttrsDatabase database) {
        this.database = database;
    }

    @Override
    public String getIdentityState() {
        return null;
    }

    @Override
    public String getMailboxState() {
        return database.mailboxDao().getState(EntityType.MAILBOX);
    }

    @NonNullDecl
    @Override
    public QueryStateWrapper getQueryState(@NullableDecl String query) {
        return database.stateDao().getQueryStateWrapper(query);
    }

    @NonNullDecl
    @Override
    public ObjectsState getObjectsState() {
        return database.stateDao().getObjectsState();
    }

    @Override
    public void setMailboxes(TypedState<Mailbox> mailboxTypedState, Mailbox[] mailboxes) {
        final List<MailboxEntity> mailboxEntities = new ArrayList<>();
        for (Mailbox mailbox : mailboxes) {
            mailboxEntities.add(MailboxEntity.of(mailbox));
        }
        database.mailboxDao().set(mailboxEntities, mailboxTypedState.getState());
    }

    @Override
    public void updateMailboxes(Update<Mailbox> update, String[] updatedProperties) throws CacheWriteException, CacheConflictException {
        try {
            database.mailboxDao().update(update, updatedProperties);
        } catch (IllegalArgumentException e) {
            throw new CacheWriteException(e);
        }
    }

    @Override
    public Collection<? extends IdentifiableMailboxWithRole> getSpecialMailboxes() throws NotSynchronizedException {
        return database.mailboxDao().getSpecialMailboxes();
    }

    @Override
    public void setThreadsAndEmails(TypedState<Thread> threadState, Thread[] threads, TypedState<Email> emailState, Email[] emails) {
        database.threadAndEmailDao().set(threadState, threads, emailState, emails);
    }

    @Override
    public void addThreadsAndEmail(TypedState<Thread> threadState, Thread[] threads, TypedState<Email> emailState, Email[] emails) {
        database.threadAndEmailDao().add(threadState,threads, emailState, emails);
    }


    @Override
    public void updateThreads(Update<Thread> update) throws CacheWriteException {
        database.threadAndEmailDao().update(update);
        Log.d("lttrs", "updated some threads "+update.toString());
    }


    @Override
    public void updateEmails(Update<Email> update, String[] updatedProperties) throws CacheWriteException {
        database.threadAndEmailDao().updateEmails(update, updatedProperties);
    }

    @Override
    public void setIdentities(TypedState<Identity> identityTypedState, Identity[] identities) {

    }

    @Override
    public void updateIdentities(Update<Identity> update) throws CacheWriteException {

    }

    @Override
    public void setQueryResult(String queryString, QueryResult queryResult) {
        database.queryDao().set(queryString, queryResult);
        Log.d("lttrs", "setting query result for query string '" + queryString + "'");
    }

    @Override
    public void addQueryResult(String queryString, QueryResult queryResult) throws CacheWriteException, CacheConflictException {
        database.queryDao().add(queryString, queryResult);
    }

    @Override
    public void updateQueryResults(String queryString, QueryUpdate<Email, QueryResultItem> queryUpdate, TypedState<Email> emailTypedState) throws CacheWriteException, CacheConflictException {
        Log.d("lttrs", "updating query results "+queryUpdate);
        database.queryDao().updateQueryResults(queryString, queryUpdate, emailTypedState);
    }

    @Override
    public Missing getMissing(String query) throws CacheReadException {
        Missing missing = database.threadAndEmailDao().getMissing(query);
        Log.d("lttrs", "cache reported " + missing.threadIds.size() + " missing threads");
        return missing;
    }
}
