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

import java.util.Date;

import androidx.room.Delete;
import androidx.room.TypeConverter;
import rs.ltt.android.entity.EmailAddressType;
import rs.ltt.android.entity.EmailBodyPartType;
import rs.ltt.android.entity.EntityType;
import rs.ltt.jmap.common.entity.Role;

public class Converters {


    @TypeConverter
    public static String toString(final Role role) {
        return role == null ? null : role.toString();
    }

    @TypeConverter
    public static Role toRole(String role) {
        return role == null ? null : Role.valueOf(role);
    }



    @TypeConverter
    public static String toString(EntityType entityType) {
        return entityType.toString();
    }

    @TypeConverter
    public static EntityType toEntityType(String entityType) {
        return EntityType.valueOf(entityType);
    }



    @TypeConverter
    public static EmailAddressType toEmailAddressType(String type) {
        return EmailAddressType.valueOf(type);
    }

    @TypeConverter
    public static String toString(EmailAddressType type) {
        return type.toString();
    }



    @TypeConverter
    public static EmailBodyPartType toEmailBodyPartType(String type) {
        return EmailBodyPartType.valueOf(type);
    }

    @TypeConverter
    public static String toString(EmailBodyPartType type) {
        return type.toString();
    }

    @TypeConverter
    public static Date toDate(long timestamp) {
        return new Date(timestamp);
    }

    @TypeConverter
    public static long toTimestamp(Date date) {
        return date.getTime();
    }
}
