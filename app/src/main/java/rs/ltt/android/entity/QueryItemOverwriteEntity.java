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

package rs.ltt.android.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "query_item_overwrite",
        primaryKeys = {"queryId", "threadId"},
        foreignKeys = {
                @ForeignKey(entity = ThreadEntity.class,
                        parentColumns = {"threadId"},
                        childColumns = {"threadId"},
                        onDelete = CASCADE
                ),
                @ForeignKey(entity = QueryEntity.class,
                        parentColumns = {"id"},
                        childColumns = {"queryId"},
                        onDelete = CASCADE)})
public class QueryItemOverwriteEntity {

    @NonNull
    public Long queryId;
    @NonNull
    public String threadId;

    public boolean executed = false;


    public QueryItemOverwriteEntity(@NonNull Long queryId, @NonNull String threadId) {
        this.queryId = queryId;
        this.threadId = threadId;
    }
}
