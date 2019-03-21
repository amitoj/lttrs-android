package rs.ltt.android.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import rs.ltt.android.Credentials;
import rs.ltt.android.cache.DatabaseCache;
import rs.ltt.android.database.LttrsDatabase;
import rs.ltt.jmap.client.session.SessionFileCache;
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
}
