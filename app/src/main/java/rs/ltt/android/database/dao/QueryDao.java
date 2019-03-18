package rs.ltt.android.database.dao;

import android.util.Log;

import java.util.List;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import rs.ltt.android.entity.EntityType;
import rs.ltt.android.entity.QueryEntity;
import rs.ltt.android.entity.QueryItemEntity;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.jmap.common.entity.AddedItem;
import rs.ltt.jmap.common.entity.Email;
import rs.ltt.jmap.common.entity.TypedState;
import rs.ltt.jmap.mua.cache.CacheConflictException;
import rs.ltt.jmap.mua.cache.QueryUpdate;
import rs.ltt.jmap.mua.entity.QueryResultItem;

@Dao
public abstract class QueryDao extends AbstractEntityDao<Email> {


    @Insert
    abstract long insert(QueryEntity entity);

    @Insert
    abstract void insert(List<QueryItemEntity> entities);

    @Insert
    abstract void insert(QueryItemEntity entity);

    //we inner join on threads here to make sure that we only return items that we actually have
    //due to the delay of fetchMissing we might have query_items that we do not have a corresponding thread for
    @Transaction
    @Query("select position,query_item.threadId from `query` join query_item on `query`.id = query_item.queryId inner join thread on query_item.threadId=thread.threadId where queryString=:queryString order by position asc")
    public abstract DataSource.Factory<Integer,ThreadOverviewItem> getThreadOverviewItems(String queryString);

    @Transaction
    public void set(String queryString, String queryState, QueryResultItem[] items, TypedState<Email> emailState) {
        throwOnCacheConflict(EntityType.EMAIL, emailState);
        //TODO delete old
        long queryId = insert(QueryEntity.of(queryString, queryState));
        insert(QueryItemEntity.of(queryId, items));
    }

    @Query("select * from `query` where queryString=:queryString")
    abstract QueryEntity getQueryEntity(String queryString);

    @Query("update query_item set position=position+1 where queryId=:queryId and position>=:position ")
    abstract void incrementAllPositionsFrom(Long queryId, Integer position);

    //TODO: is this query safe to run when emailId is not found
    @Query("update query_item set position=position-1 where queryId=:queryId and position>(select position from query_item where emailId=:emailId and queryId=:queryId)")
    abstract void decrementAllPositionsFrom(Long queryId, String emailId);

    @Query("delete from query_item where queryId=:queryId and emailId=:emailId")
    abstract void deleteQueryItem(Long queryId, String emailId);

    @Query("update `query` set state=:newState where state=:oldState and id=:queryId")
    abstract int updateQueryState(Long queryId, String newState, String oldState);

    @Transaction
    public void updateQueryResults(String queryString, QueryUpdate<Email, QueryResultItem> queryUpdate, final TypedState<Email> emailState) {
        throwOnCacheConflict(EntityType.EMAIL, emailState);
        final QueryEntity queryEntity = getQueryEntity(queryString);
        for (String emailId : queryUpdate.getRemoved()) {
            Log.d("lttrs", "delting emailId=" + emailId + " from queryId=" + queryEntity.id);
            decrementAllPositionsFrom(queryEntity.id, emailId);
            deleteQueryItem(queryEntity.id, emailId);
        }
        for (AddedItem<QueryResultItem> addedItem : queryUpdate.getAdded()) {
            Log.d("lttrs", "adding item " + addedItem);
            Log.d("lttrs", "increment all positions where queryId=" + queryEntity.id + " and position=" + addedItem.getIndex());

            //TODO if the number of changes is 0 it means the added item is out of range and should be ignored
            incrementAllPositionsFrom(queryEntity.id, addedItem.getIndex());
            Log.d("lttrs", "insert queryItemEntity on position " + addedItem.getIndex() + " and id=" + queryEntity.id);
            insert(QueryItemEntity.of(queryEntity.id, addedItem.getIndex(), addedItem.getItem()));
        }

        final String newState = queryUpdate.getNewTypedState().getState();
        final String oldState = queryUpdate.getOldTypedState().getState();
        if (updateQueryState(queryEntity.id, newState, oldState) != 1) {
            throw new CacheConflictException("Unable to update query from oldState=" + oldState + " to newState=" + newState);
        }
    }
}
