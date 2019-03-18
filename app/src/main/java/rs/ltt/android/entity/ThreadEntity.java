package rs.ltt.android.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import rs.ltt.jmap.common.entity.Thread;

@Entity(tableName = "thread")
public class ThreadEntity {

    @PrimaryKey
    @NonNull
    public String threadId;


    public ThreadEntity(@NonNull String threadId) {
        this.threadId = threadId;
    }

    public static ThreadEntity of(Thread thread) {
        return new ThreadEntity(thread.getId());
    }

    public static ThreadEntity of(String id) {
        return new ThreadEntity(id);
    }
}
