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

import com.google.common.base.Objects;

import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import rs.ltt.jmap.common.entity.Role;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "mailbox_overwrite",
        primaryKeys = {"threadId", "name", "role"},
        foreignKeys = @ForeignKey(entity = ThreadEntity.class,
                parentColumns = {"threadId"},
                childColumns = {"threadId"},
                onDelete = CASCADE
        )
)
public class MailboxOverwriteEntity {

    @NonNull
    public String threadId;
    @NonNull
    public String name;
    @NonNull
    public String role;
    public boolean value;

    public static MailboxOverwriteEntity of(String threadId, @NonNull Role role, boolean value) {
        MailboxOverwriteEntity entity = new MailboxOverwriteEntity();
        entity.threadId = threadId;
        entity.role = role.toString();
        entity.name = "";
        entity.value = value;
        return entity;
    }


    public static MailboxOverwriteEntity of(String threadId, @NonNull String label, boolean value) {
        MailboxOverwriteEntity entity = new MailboxOverwriteEntity();
        entity.threadId = threadId;
        entity.role = "";
        entity.name = label;
        entity.value = value;
        return entity;
    }


    public static boolean hasOverwrite(Collection<MailboxOverwriteEntity> overwriteEntities, Role role) {
        for (MailboxOverwriteEntity overwriteEntity : overwriteEntities) {
            if (role.toString().equals(overwriteEntity.role)) {
                return overwriteEntity.value;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailboxOverwriteEntity entity = (MailboxOverwriteEntity) o;
        return value == entity.value &&
                Objects.equal(threadId, entity.threadId) &&
                Objects.equal(name, entity.name) &&
                Objects.equal(role, entity.role);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(threadId, name, role, value);
    }
}
