package rs.ltt.android.database.dao;

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

    @Delete
    abstract void delete(ThreadEntity thread);

    @Transaction
    public void set(Thread[] threads, EntityStateEntity entityState) {
        //TODO delete old
        insertThreads(threads);
        insert(entityState);
    }

    @Transaction
    public void add(final TypedState<Thread> expectedState, Thread[] threads) {
        insertThreads(threads);
        throwOnCacheConflict(EntityType.THREAD, expectedState);
    }

    private void insertThreads(Thread[] threads) {
        for (Thread thread : threads) {
            insert(ThreadEntity.of(thread));
            insert(ThreadItemEntity.of(thread));
        }
    }

    @Transaction
    public void update(Update<Thread> update) {
        insertThreads(update.getCreated());
        for (Thread thread : update.getUpdated()) {
            delete(ThreadEntity.of(thread));
            insert(ThreadEntity.of(thread));
            insert(ThreadItemEntity.of(thread));
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
