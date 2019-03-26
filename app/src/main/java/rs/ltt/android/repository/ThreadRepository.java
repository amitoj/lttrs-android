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

package rs.ltt.android.repository;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import rs.ltt.android.entity.FullEmail;
import rs.ltt.android.entity.MailboxWithRoleAndName;
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

    public LiveData<List<MailboxWithRoleAndName>> getMailboxes(String threadId) {
        return database.mailboxDao().getMailboxesForThread(threadId);
    }
}
