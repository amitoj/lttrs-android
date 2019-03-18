package rs.ltt.android.database.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import rs.ltt.android.entity.EmailEmailAddressEntity;
import rs.ltt.android.entity.EmailEntity;
import rs.ltt.android.entity.EmailKeywordEntity;
import rs.ltt.android.entity.EmailMailboxEntity;
import rs.ltt.android.entity.EntityStateEntity;
import rs.ltt.android.entity.EntityType;
import rs.ltt.jmap.common.entity.Email;
import rs.ltt.jmap.common.entity.TypedState;
import rs.ltt.jmap.mua.cache.Update;

@Dao
public abstract class EmailDao extends AbstractEntityDao<Email> {

    @Query("delete from email where id=:id")
    abstract void deleteEmail(String id);

    @Query("delete from email_keyword where emailId=:emailId")
    abstract void deleteKeywords(String emailId);

    @Query("delete from email_mailbox where emailId=:emailId")
    abstract void deleteMailboxes(String emailId);

    @Insert
    abstract void insert(EmailEntity entity);

    @Insert
    abstract void insertEmailAddresses(List<EmailEmailAddressEntity> entities);

    @Insert
    abstract void insertMailboxes(List<EmailMailboxEntity> entities);

    @Insert
    abstract void insertKeywords(List<EmailKeywordEntity> entities);

    @Transaction
    public void set(final Email[] emails, EntityStateEntity entityState) {
        //TODO delete old email
        insertEmails(emails);
        insert(entityState);
    }

    @Transaction
    public void add(final TypedState<Email> expectedState, Email[] email) {
        insertEmails(email);
        throwOnCacheConflict(EntityType.EMAIL, expectedState);
    }

    private void insertEmails(final Email[] emails) {
        for (Email email : emails) {
            insert(EmailEntity.of(email));
            insertEmailAddresses(EmailEmailAddressEntity.of(email));
            insertMailboxes(EmailMailboxEntity.of(email));
            insertKeywords(EmailKeywordEntity.of(email));
        }
    }

    public void updateEmails(Update<Email> update, String[] updatedProperties) {
        insertEmails(update.getCreated());
        if (updatedProperties != null) {
            for (Email email : update.getUpdated()) {
                for (String property : updatedProperties) {
                    switch (property) {
                        case "keywords":
                            deleteKeywords(email.getId());
                            insertKeywords(EmailKeywordEntity.of(email));
                            break;
                        case "mailboxIds":
                            deleteMailboxes(email.getId());
                            insertMailboxes(EmailMailboxEntity.of(email));
                            break;
                        default:
                            throw new IllegalArgumentException("Unable to update property '" + property + "'");
                    }
                }
            }
        }
        for (String id : update.getDestroyed()) {
            deleteEmail(id);
        }
        throwOnUpdateConflict(EntityType.EMAIL, update.getOldTypedState(), update.getNewTypedState());
    }
}
