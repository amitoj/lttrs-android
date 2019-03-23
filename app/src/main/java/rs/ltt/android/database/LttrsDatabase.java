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

package rs.ltt.android.database;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import rs.ltt.android.database.dao.EmailDao;
import rs.ltt.android.database.dao.MailboxDao;
import rs.ltt.android.database.dao.OverwriteDao;
import rs.ltt.android.database.dao.QueryDao;
import rs.ltt.android.database.dao.StateDao;
import rs.ltt.android.database.dao.ThreadDao;
import rs.ltt.android.entity.EmailBodyPartEntity;
import rs.ltt.android.entity.EmailBodyValueEntity;
import rs.ltt.android.entity.EmailEmailAddressEntity;
import rs.ltt.android.entity.EmailEntity;
import rs.ltt.android.entity.EmailKeywordEntity;
import rs.ltt.android.entity.EmailMailboxEntity;
import rs.ltt.android.entity.EntityStateEntity;
import rs.ltt.android.entity.KeywordOverwriteEntity;
import rs.ltt.android.entity.MailboxEntity;
import rs.ltt.android.entity.QueryEntity;
import rs.ltt.android.entity.QueryItemEntity;
import rs.ltt.android.entity.QueryItemOverwriteEntity;
import rs.ltt.android.entity.ThreadEntity;
import rs.ltt.android.entity.ThreadItemEntity;

@Database(entities = {MailboxEntity.class,
        EntityStateEntity.class,
        ThreadEntity.class,
        ThreadItemEntity.class,
        EmailEntity.class,
        EmailEmailAddressEntity.class,
        EmailKeywordEntity.class,
        EmailMailboxEntity.class,
        EmailBodyValueEntity.class,
        EmailBodyPartEntity.class,
        QueryEntity.class,
        QueryItemEntity.class,
        KeywordOverwriteEntity.class,
        QueryItemOverwriteEntity.class
}, version = 1)
@TypeConverters(Converters.class)
public abstract class LttrsDatabase extends RoomDatabase {

    private static Map<String, LttrsDatabase> INSTANCES = new HashMap<>();

    public abstract EmailDao emailDao();

    public abstract ThreadDao threadDao();

    public abstract MailboxDao mailboxDao();

    public abstract StateDao stateDao();

    public abstract QueryDao queryDao();

    public abstract OverwriteDao overwriteDao();

    public static LttrsDatabase getInstance(final Context context, final String account) {
        final LttrsDatabase instance = INSTANCES.get(account);
        if (instance != null) {
            return instance;
        }
        synchronized (LttrsDatabase.class) {
            LttrsDatabase inner = INSTANCES.get(account);
            if (inner == null) {
                inner = Room.databaseBuilder(context.getApplicationContext(), LttrsDatabase.class, "lttrs-" + account).build();
                INSTANCES.put(account, inner);
            }
            return inner;
        }
    }
}
