package rs.ltt.android.entity;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import rs.ltt.jmap.common.entity.Email;

@Entity(tableName = "email")
public class EmailEntity {


    @NonNull
    @PrimaryKey
    public String id;

    public String blobId;

    public String threadId;

    public Integer size;

    public Date receivedAt;

    public String subject;

    public Date sentAt;

    public Boolean hasAttachment;

    public String preview;

    public static EmailEntity of(Email email) {
        final EmailEntity entity = new EmailEntity();
        entity.id = email.getId();
        entity.blobId = email.getBlobId();
        entity.threadId = email.getThreadId();
        entity.size = email.getSize();
        entity.receivedAt = email.getReceivedAt();
        entity.subject = email.getSubject();
        entity.sentAt = email.getSentAt();
        entity.hasAttachment = email.getHasAttachment();
        entity.preview = email.getPreview();
        return entity;
    }

}
