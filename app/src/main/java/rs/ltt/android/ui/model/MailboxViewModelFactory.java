package rs.ltt.android.ui.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MailboxViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final String id;

    public MailboxViewModelFactory(Application application, String id) {
        this.application = application;
        this.id = id;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return modelClass.cast(new MailboxQueryViewModel(application, id));
    }
}
