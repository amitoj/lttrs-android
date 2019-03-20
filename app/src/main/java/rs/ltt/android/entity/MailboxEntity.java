package rs.ltt.android.entity;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import rs.ltt.jmap.common.entity.IdentifiableSpecialMailbox;
import rs.ltt.jmap.common.entity.Mailbox;
import rs.ltt.jmap.common.entity.Role;

@Entity(tableName = "mailbox")
public class MailboxEntity implements IdentifiableSpecialMailbox {

    @NonNull
    @PrimaryKey
    public String id;

    public String name;

    public String parentId;

    public Role role;

    public Integer sortOrder;

    public Integer totalEmails;

    public Integer unreadEmails;

    public Integer totalThreads;

    public Integer unreadThreads;

    @Embedded
    public MailboxRightsEmbed myRights;

    public Boolean isSubscribed;


    @Override
    @NonNull
    public String getId() {
        return this.id;
    }

    @Override
    public Role getRole() {
        return role;
    }

    public static MailboxEntity of(final Mailbox mailbox) {
        final MailboxEntity entity = new MailboxEntity();
        entity.id = mailbox.getId();
        entity.name = mailbox.getName();
        entity.parentId = mailbox.getParentId();
        entity.role = mailbox.getRole();
        entity.sortOrder = mailbox.getSortOrder();
        entity.totalEmails = mailbox.getTotalEmails();
        entity.unreadEmails = mailbox.getUnreadEmails();
        entity.totalThreads = mailbox.getTotalThreads();
        entity.unreadThreads = mailbox.getUnreadThreads();
        entity.myRights = MailboxRightsEmbed.of(mailbox.getMyRights());
        entity.isSubscribed = mailbox.getIsSubscribed();
        return entity;
    }
}
