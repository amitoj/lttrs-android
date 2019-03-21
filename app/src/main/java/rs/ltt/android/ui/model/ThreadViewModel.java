package rs.ltt.android.ui.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import rs.ltt.android.Credentials;
import rs.ltt.android.database.LttrsDatabase;
import rs.ltt.android.entity.FullEmail;

public class ThreadViewModel extends AndroidViewModel {

    private final String threadId;

    private LiveData<PagedList<FullEmail>> emails;


    public ThreadViewModel(@NonNull Application application, String threadId) {
        super(application);
        this.threadId = threadId;
        LttrsDatabase lttrsDatabase = LttrsDatabase.getInstance(application, Credentials.username);
        this.emails = new LivePagedListBuilder<>(lttrsDatabase.emailDao().getThread(threadId), 30).build();
    }

    public LiveData<PagedList<FullEmail>> getEmails() {
        return emails;
    }
}
