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

import java.util.Arrays;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;
import rs.ltt.android.entity.EntityState;
import rs.ltt.android.entity.EntityType;
import rs.ltt.jmap.mua.cache.ObjectsState;
import rs.ltt.jmap.mua.cache.QueryStateWrapper;

@Dao
public abstract class StateDao {

    @Query("select state,type from entity_state where type in (:types)")
    public abstract List<EntityState> getEntityStates(List<EntityType> types);

    public ObjectsState getObjectsState() {
        List<EntityState> entityStates = getEntityStates(Arrays.asList(EntityType.EMAIL, EntityType.MAILBOX, EntityType.THREAD));
        String mailboxState = null;
        String threadState = null;
        String emailState = null;
        for (EntityState entityState : entityStates) {
            switch (entityState.type) {
                case MAILBOX:
                    mailboxState = entityState.state;
                    break;
                case THREAD:
                    threadState = entityState.state;
                    break;
                case EMAIL:
                    emailState = entityState.state;
                    break;
            }
        }
        return new ObjectsState(mailboxState, threadState, emailState);
    }

    @Query("select state from `query` where queryString=:queryString")
    abstract String getQueryState(String queryString);

    @Query("select emailId from `query` join query_item on `query`.id = queryId  where queryString=:queryString order by position desc limit 1")
    abstract String getUpTo(String queryString);

    @Transaction
    public QueryStateWrapper getQueryStateWrapper(String queryString) {
        final String queryState = getQueryState(queryString);
        final ObjectsState objectsState = getObjectsState();
        final String upTo;
        if (queryState == null) {
            upTo = null;
        } else {
            upTo = getUpTo(queryString);
        }
        return new QueryStateWrapper(queryState, upTo, objectsState);
    }
}
