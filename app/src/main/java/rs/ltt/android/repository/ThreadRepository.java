package rs.ltt.android.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import rs.ltt.android.entity.FullEmail;
import rs.ltt.android.entity.ThreadHeader;

public class ThreadRepository extends LttrsRepository {

    public ThreadRepository(Application application) {
        super(application);
    }

    public LiveData<PagedList<FullEmail>> getEmails(String threadId) {
        return new LivePagedListBuilder<>(database.emailDao().getEmails(threadId), 30).build();
    }

    public LiveData<ThreadHeader> getThreadHeader(String threadId) {
        return database.emailDao().getThreadHeader(threadId);
    }
}
