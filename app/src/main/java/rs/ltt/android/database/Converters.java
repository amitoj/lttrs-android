package rs.ltt.android.database;

import java.util.Date;

import androidx.room.TypeConverter;
import rs.ltt.android.entity.EmailAddressType;
import rs.ltt.android.entity.EntityType;
import rs.ltt.jmap.common.entity.Role;

public class Converters {


    @TypeConverter
    public static String toString(Role role) {
        return role.toString();
    }

    @TypeConverter
    public static Role toRole(String role) {
        return Role.valueOf(role);
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
    public static Date toDate(long timestamp) {
        return new Date(timestamp);
    }

    @TypeConverter
    public static long toTimestamp(Date date) {
        return date.getTime();
    }
}
