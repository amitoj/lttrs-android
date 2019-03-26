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

package rs.ltt.android.worker;

import android.content.Context;
import android.util.Log;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;
import rs.ltt.android.entity.EmailWithMailboxes;

public class MoveToTrashWorker extends MuaWorker {

    private static final String THREAD_ID_KEY = "threadId";

    private final String threadId;

    public MoveToTrashWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        final Data data = getInputData();
        this.threadId = data.getString(THREAD_ID_KEY);
    }

    @NonNull
    @Override
    public Result doWork() {
        List<EmailWithMailboxes> emails = threadId == null ? Collections.emptyList() : database.emailDao().getEmailsWithMailboxes(threadId);
        Log.d("lttrs", "MoveToTrash threadId=" + threadId + " ("+emails.size()+")");
        try {
            Boolean result = mua.moveToTrash(emails).get();
            Log.d("lttrs", "made changes to " + threadId + ": " + result);
            return Result.success();
        } catch (ExecutionException e) {
            return toResult(e);
        } catch (InterruptedException e) {
            return Result.failure();
        }
    }

    public static Data data(String threadId) {
        return new Data.Builder()
                .putString(THREAD_ID_KEY, threadId)
                .build();
    }
}
