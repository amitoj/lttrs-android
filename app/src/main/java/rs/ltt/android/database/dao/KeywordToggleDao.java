package rs.ltt.android.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import rs.ltt.android.entity.KeywordOverwriteEntity;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public abstract class KeywordToggleDao {

    @Insert(onConflict = REPLACE)
    public abstract void insert(KeywordOverwriteEntity keywordToggle);
}
