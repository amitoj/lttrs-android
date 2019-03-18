package rs.ltt.android.worker;

import android.content.Context;

import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;
import rs.ltt.jmap.common.entity.EmailQuery;
import rs.ltt.jmap.common.entity.filter.EmailFilterCondition;
import rs.ltt.jmap.mua.Mua;

public class QueryRefreshWorker extends MuaWorker {

    public QueryRefreshWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        final EmailFilterCondition.EmailFilterConditionBuilder emailFilterConditionBuilder = EmailFilterCondition.builder();
        final Data data = getInputData();
        final boolean collapseThreads = data.getBoolean("collapse_threads", true);
        final String mailbox = data.getString("mailbox");

        if (mailbox != null) {
            emailFilterConditionBuilder.inMailbox(mailbox);
        }

        EmailQuery query = EmailQuery.of(emailFilterConditionBuilder.build(), collapseThreads);

        Mua mua = createMua();
        try {
            mua.query(query).get();
            return Result.success();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                cause.printStackTrace();;
            } else {
                e.printStackTrace();
            }
            return Result.failure();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
