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

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import rs.ltt.android.database.dao.SearchSuggestionDao;
import rs.ltt.android.entity.SearchSuggestionEntity;

@Database(entities = {
        SearchSuggestionEntity.class
}, version = 1)
public abstract class AppDatabase extends RoomDatabase {


    private static volatile AppDatabase INSTANCE = null;

    public abstract SearchSuggestionDao searchSuggestionDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        synchronized (AppDatabase.class) {
            if (INSTANCE != null) {
                return INSTANCE;
            }
            INSTANCE = Room.databaseBuilder(context,AppDatabase.class,"app").build();
            return INSTANCE;
        }
    }

}
