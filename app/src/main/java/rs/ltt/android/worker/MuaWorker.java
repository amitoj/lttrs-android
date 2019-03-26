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

import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import rs.ltt.android.Credentials;
import rs.ltt.android.cache.DatabaseCache;
import rs.ltt.android.database.LttrsDatabase;
import rs.ltt.jmap.client.api.MethodErrorResponseException;
import rs.ltt.jmap.client.session.SessionFileCache;
import rs.ltt.jmap.common.method.MethodErrorResponse;
import rs.ltt.jmap.common.method.error.StateMismatchMethodErrorResponse;
import rs.ltt.jmap.mua.Mua;

public abstract class MuaWorker extends Worker {

    public static final String SYNC = "sync";

    protected final LttrsDatabase database;
    protected final Mua mua;

    MuaWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.database = LttrsDatabase.getInstance(getApplicationContext(), Credentials.username);
        this.mua = Mua.builder()
                .password(Credentials.password)
                .username(Credentials.username)
                .cache(new DatabaseCache(this.database))
                .sessionCache(new SessionFileCache(getApplicationContext().getCacheDir()))
                .queryPageSize(20)
                .build();
    }

    protected static Result toResult(ExecutionException e) {
        final Throwable cause = e.getCause();
        if (cause != null) {
            if (cause instanceof MethodErrorResponseException) {
                MethodErrorResponse methodErrorResponse = ((MethodErrorResponseException) cause).getMethodErrorResponse();
                if (methodErrorResponse instanceof StateMismatchMethodErrorResponse) {
                    Log.d("lttrs", "state mismatch; try again");
                    return Result.retry();
                }
                if (methodErrorResponse != null) {
                    Log.d("lttrs", "method error response " + methodErrorResponse.getType(), e);
                } else {
                    Log.d("lttrs", e.getMessage(), e);
                }
            }
        }
        return Result.failure();
    }
}
