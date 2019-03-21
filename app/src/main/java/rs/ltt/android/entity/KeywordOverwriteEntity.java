package rs.ltt.android.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "keyword_overwrite",
        primaryKeys = {"threadId","keyword"},
        foreignKeys = @ForeignKey(entity = ThreadEntity.class,
                parentColumns = {"threadId"},
                childColumns = {"threadId"},
                onDelete = CASCADE
        )
)
public class KeywordOverwriteEntity {

    @NonNull public String threadId;
    @NonNull public String keyword;
    public boolean value;

    public KeywordOverwriteEntity(@NonNull String threadId, @NonNull String keyword, boolean value) {
        this.threadId = threadId;
        this.keyword = keyword;
        this.value = value;
    }
}
