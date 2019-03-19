package rs.ltt.android.database.dao;

import android.util.Log;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Ignore;
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

import static androidx.room.OnConflictStrategy.IGNORE;

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

    @Insert()
    abstract void insertMailboxes(List<EmailMailboxEntity> entities);

    @Insert()
    abstract void insertKeywords(List<EmailKeywordEntity> entities);

    @Transaction
    public void set(final Email[] emails, EntityStateEntity entityState) {
        //TODO delete old email
        if (emails.length > 0) {
            insertEmails(emails);
        }
        insert(entityState);
    }

    @Transaction
    public void add(final TypedState<Email> expectedState, Email[] email) {
        if (email.length > 0) {
            insertEmails(email);
        }
        throwOnCacheConflict(EntityType.EMAIL, expectedState);
    }

    @Query("SELECT EXISTS(SELECT 1 FROM email WHERE id=:emailId)")
    protected abstract boolean exists(String emailId);

    private void insertEmails(final Email[] emails) {
        for (Email email : emails) {
            insert(EmailEntity.of(email));
            insertEmailAddresses(EmailEmailAddressEntity.of(email));
            insertMailboxes(EmailMailboxEntity.of(email));
            insertKeywords(EmailKeywordEntity.of(email));
        }
    }

    public void updateEmails(Update<Email> update, String[] updatedProperties) {
        final String newState = update.getNewTypedState().getState();
        if (newState != null && newState.equals(getState(EntityType.EMAIL))) {
            Log.d("lttrs","nothing to do. emails already at newest state");
            return;
        }
        final Email[] created = update.getCreated();
        if (created.length > 0) {
            insertEmails(created);
        }
        if (updatedProperties != null) {
            for (Email email : update.getUpdated()) {
                if (!exists(email.getId())) {
                    Log.d("lttrs","skipping updates to email "+email.getId()+" because we don’t have that");
                    continue;
                }
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
