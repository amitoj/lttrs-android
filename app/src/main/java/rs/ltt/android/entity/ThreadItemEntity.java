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

    public Integer position;

    public ThreadItemEntity(@NonNull String threadId, @NonNull String emailId, @NonNull Integer position) {
        this.threadId = threadId;
        this.emailId = emailId;
        this.position = position;
    }


    public int getPosition() {
        return this.position;
    }

    public static List<ThreadItemEntity> of(final Thread thread) {
        final List<ThreadItemEntity> entities = new ArrayList<>();
        List<String> emailIds = thread.getEmailIds();
        for(int i = 0; i < emailIds.size(); ++i) {
            entities.add(new ThreadItemEntity(thread.getId(), emailIds.get(i), i));
        }
        return entities;
    }
}
