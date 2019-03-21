package rs.ltt.android.ui.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ThreadViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final String threadId;

    public ThreadViewModelFactory(Application application, String threadId) {
        this.application = application;
        this.threadId = threadId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return modelClass.cast(new ThreadViewModel(application, threadId));
    }
}
