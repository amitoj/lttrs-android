package rs.ltt.android.ui.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import rs.ltt.android.entity.FullEmail;
import rs.ltt.android.entity.ThreadHeader;
import rs.ltt.android.repository.ThreadRepository;

public class ThreadViewModel extends AndroidViewModel {

    private final String threadId;

    private final ThreadRepository threadRepository;

    private LiveData<PagedList<FullEmail>> emails;

    private LiveData<ThreadHeader> header;


    ThreadViewModel(@NonNull Application application, String threadId) {
        super(application);
        this.threadId = threadId;
        this.threadRepository = new ThreadRepository(application);
        this.header = this.threadRepository.getThreadHeader(threadId);
        this.emails = this.threadRepository.getEmails(threadId);
    }

    public LiveData<PagedList<FullEmail>> getEmails() {
        return emails;
    }

    public LiveData<ThreadHeader> getHeader() {
        return this.header;
    }

    public void toggleFlagged(String threadId, boolean target) {
        threadRepository.toggleFlagged(threadId, target);
    }
}
