/*
 * Copyright 2019 Daniel Gultsch
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
