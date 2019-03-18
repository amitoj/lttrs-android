package rs.ltt.android.entity;

import com.google.common.collect.ImmutableList;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import rs.ltt.jmap.common.entity.Email;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "email_mailbox",
        primaryKeys = {"emailId", "mailboxId"},
        foreignKeys = @ForeignKey(entity = EmailEntity.class,
                parentColumns = {"id"},
                childColumns = {"emailId"},
                onDelete = CASCADE
        )
)
public class EmailMailboxEntity {

    @NonNull
    public String emailId;

    @NonNull
    public String mailboxId;

    public EmailMailboxEntity(@NonNull String emailId, @NonNull String mailboxId) {
        this.emailId = emailId;
        this.mailboxId = mailboxId;
    }

    public static List<EmailMailboxEntity> of(Email email) {
        ImmutableList.Builder<EmailMailboxEntity> builder = new ImmutableList.Builder<>();
        for(String mailboxId : email.getMailboxIds().keySet()) {
            builder.add(new EmailMailboxEntity(email.getId(),mailboxId));
        }
        return builder.build();
    }

}
