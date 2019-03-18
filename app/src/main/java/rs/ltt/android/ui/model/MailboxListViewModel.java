package rs.ltt.android.ui.model;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import rs.ltt.android.Credentials;
import rs.ltt.android.database.LttrsDatabase;
import rs.ltt.android.entity.MailboxOverviewItem;

public class MailboxListViewModel extends AndroidViewModel {

    private final LiveData<List<MailboxOverviewItem>> mailboxes;


    public MailboxListViewModel(@NonNull Application application) {
        super(application);
        this.mailboxes = LttrsDatabase.getInstance(application.getApplicationContext(), Credentials.username).mailboxDao().getMailboxes();
    }

    public LiveData<List<MailboxOverviewItem>> getMailboxes() {
        return this.mailboxes;
    }
}
