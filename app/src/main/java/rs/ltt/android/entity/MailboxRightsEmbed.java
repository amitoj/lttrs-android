package rs.ltt.android.entity;

import rs.ltt.jmap.common.entity.MailboxRights;

public class MailboxRightsEmbed {

    public static MailboxRightsEmbed of(MailboxRights mailboxRights) {
        final MailboxRightsEmbed mailboxRightsEmbed = new MailboxRightsEmbed();
        return mailboxRightsEmbed;
    }
}
