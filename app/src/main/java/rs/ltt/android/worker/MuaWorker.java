package rs.ltt.android.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import rs.ltt.android.Credentials;
import rs.ltt.android.cache.DatabaseCache;
import rs.ltt.android.database.LttrsDatabase;
import rs.ltt.jmap.mua.Mua;

public abstract class MuaWorker extends Worker {

    public static final String USER_INITIATED_REFRESH = "user_initiated_refresh";

    MuaWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    Mua createMua() {
        return Mua.builder()
                .username(Credentials.username)
                .password(Credentials.password)
                .cache(new DatabaseCache(LttrsDatabase.getInstance(getApplicationContext(),Credentials.username)))
                .build();
    }
}
