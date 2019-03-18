package rs.ltt.android.entity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import rs.ltt.jmap.common.entity.Thread;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "thread_item",
        primaryKeys = {"threadId","emailId"},
        foreignKeys = @ForeignKey(entity = ThreadEntity.class,
                parentColumns = {"threadId"},
                childColumns = {"threadId"},
                onDelete = CASCADE
        )
)
public class ThreadItemEntity {
    @NonNull
    public String threadId;
    @NonNull
    public String emailId;

    public ThreadItemEntity(@NonNull String threadId, @NonNull String emailId) {
        this.threadId = threadId;
        this.emailId = emailId;
    }


    public static List<ThreadItemEntity> of(final Thread thread) {
        final List<ThreadItemEntity> entities = new ArrayList<>();
        for (final String emailId : thread.getEmailIds()) {
            entities.add(new ThreadItemEntity(thread.getId(), emailId));
        }
        return entities;
    }
}
