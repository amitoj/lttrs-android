package rs.ltt.android.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "query",
indices = {@Index(value = {"queryString"}, unique = true)})
public class QueryEntity {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    public String queryString;
    public String state;

    public QueryEntity(String queryString, String state) {
        this.queryString = queryString;
        this.state = state;
    }


    public static QueryEntity of(String queryString, String state) {
        return new QueryEntity(queryString, state);
    }
}
