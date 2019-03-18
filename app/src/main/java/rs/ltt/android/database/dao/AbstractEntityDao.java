package rs.ltt.android.database.dao;

import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import rs.ltt.android.entity.EntityStateEntity;
import rs.ltt.android.entity.EntityType;
import rs.ltt.jmap.common.entity.AbstractIdentifiableEntity;
import rs.ltt.jmap.common.entity.TypedState;
import rs.ltt.jmap.mua.cache.CacheConflictException;

public abstract class AbstractEntityDao<T extends AbstractIdentifiableEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insert(EntityStateEntity entityStateEntity);

    @Query("select state from entity_state where type=:type")
    public abstract String getState(EntityType type);

    @Query("update entity_state set state=:newState where type=:type and state=:oldState")
    protected abstract int updateState(EntityType type, String oldState, String newState);

    void throwOnCacheConflict(EntityType type, TypedState<T> expectedTypedState) {
        final String expectedState = expectedTypedState.getState();
        final String currentState = getState(type);
        if (expectedState == null || !expectedState.equals(currentState)) {
            throw new CacheConflictException(type.toString()+" state was '" + currentState + "'. Expected '" + expectedState + "'");
        }
    }

    void throwOnUpdateConflict(final EntityType type, final TypedState<T> oldTypedState, final TypedState<T> newTypedState) {
        final String oldState = oldTypedState.getState();
        final String newState = newTypedState.getState();
        if (updateState(type, oldState, newState) != 1) {
            throw new CacheConflictException("Unable to update from oldState="+oldState+" to newState="+newState);
        }
    }
}
