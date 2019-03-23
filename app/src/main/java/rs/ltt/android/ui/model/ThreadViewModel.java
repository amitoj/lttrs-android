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

import java.util.HashSet;

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

    public final HashSet<String> expandedItems = new HashSet<>();


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
