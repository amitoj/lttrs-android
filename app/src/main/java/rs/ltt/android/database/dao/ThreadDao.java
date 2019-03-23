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

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import rs.ltt.android.entity.EntityStateEntity;
import rs.ltt.android.entity.EntityType;
import rs.ltt.android.entity.ThreadEntity;
import rs.ltt.android.entity.ThreadItemEntity;
import rs.ltt.jmap.common.entity.Thread;
import rs.ltt.jmap.common.entity.TypedState;
import rs.ltt.jmap.mua.cache.CacheConflictException;
import rs.ltt.jmap.mua.cache.Missing;
import rs.ltt.jmap.mua.cache.Update;

@Dao
public abstract class ThreadDao extends AbstractEntityDao<Thread> {

    @Insert
    abstract void insert(ThreadEntity entity);

    @Insert
    abstract void insert(List<ThreadItemEntity> entities);

    @Query("delete from thread_item where threadId=:threadId")
    abstract void deleteAllThreadItem(String threadId);

    @Delete
    abstract void delete(ThreadEntity thread);

    @Query("delete from thread")
    abstract void deleteAll();

    @Transaction
    public void set(Thread[] threads, String state) {
        if (state != null && state.equals(getState(EntityType.THREAD))) {
            Log.d("lttrs","nothing to do. threads with this state have already been set");
            return;
        }
        deleteAll();
        if (threads.length > 0) {
            insertThreads(threads);
        }
        insert(new EntityStateEntity(EntityType.THREAD, state));
    }

    @Transaction
    public void add(final TypedState<Thread> expectedState, Thread[] threads) {
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
    protected abstract boolean exists(String threadId);

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
            if (exists(thread.getId())) {
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
}
