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

    @Transaction
    public QueryStateWrapper getQueryStateWrapper(String queryString) {
        final String queryState = getQueryState(queryString);
        final ObjectsState objectsState = getObjectsState();
        return new QueryStateWrapper(queryState, objectsState);
    }
}
