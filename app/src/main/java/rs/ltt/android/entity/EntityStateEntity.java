package rs.ltt.android.entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "entity_state")
public class EntityStateEntity {

    @NonNull
    @PrimaryKey
    public EntityType type;
    public String state;

    public EntityStateEntity(@NonNull EntityType type, String state) {
        this.type = type;
        this.state = state;
    }
}
