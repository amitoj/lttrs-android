package rs.ltt.android.entity;

import com.google.common.collect.ImmutableList;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import rs.ltt.jmap.common.entity.Email;
import rs.ltt.jmap.common.entity.EmailAddress;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "email_email_address",
        primaryKeys = {"emailId", "position", "type"},
        foreignKeys = @ForeignKey(entity = EmailEntity.class,
                parentColumns = {"id"},
                childColumns = {"emailId"},
                onDelete = CASCADE
        )
)
public class EmailEmailAddressEntity {

    @NonNull
    public String emailId;
    @NonNull
    public Integer position;
    @NonNull
    public EmailAddressType type;

    public String name;
    public String email;

    private static EmailEmailAddressEntity of(String emailId, Integer position, EmailAddressType type, EmailAddress address) {
        final EmailEmailAddressEntity entity = new EmailEmailAddressEntity();
        entity.emailId = emailId;
        entity.position = position;
        entity.type = type;
        entity.email = address.getEmail();
        entity.name = address.getName();
        return entity;
    }

    private static void addToBuilder(ImmutableList.Builder<EmailEmailAddressEntity> builder, String emailId, EmailAddressType type, List<EmailAddress> addresses) {
        if (addresses == null) {
            return;
        }
        for (int i = 0; i < addresses.size(); ++i) {
            builder.add(of(emailId, i, type, addresses.get(i)));
        }
    }

    public static List<EmailEmailAddressEntity> of(Email email) {
        ImmutableList.Builder<EmailEmailAddressEntity> builder = new ImmutableList.Builder<>();
        addToBuilder(builder, email.getId(), EmailAddressType.SENDER, email.getSender());
        addToBuilder(builder, email.getId(), EmailAddressType.FROM, email.getFrom());
        addToBuilder(builder, email.getId(), EmailAddressType.TO, email.getTo());
        addToBuilder(builder, email.getId(), EmailAddressType.CC, email.getCc());
        addToBuilder(builder, email.getId(), EmailAddressType.BCC, email.getBcc());
        addToBuilder(builder, email.getId(), EmailAddressType.REPLY_TO, email.getReplyTo());
        return builder.build();
    }

}
