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
