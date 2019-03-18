package rs.ltt.android.entity;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import rs.ltt.jmap.common.entity.Email;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "email_keyword",
        primaryKeys = {"emailId", "keyword"},
        foreignKeys = @ForeignKey(entity = EmailEntity.class,
                parentColumns = {"id"},
                childColumns = {"emailId"},
                onDelete = CASCADE
        )
)
public class EmailKeywordEntity {

    @NonNull
    public String emailId;
    @NonNull
    public String keyword;

    public EmailKeywordEntity(@NonNull String emailId, @NonNull String keyword) {
        this.emailId = emailId;
        this.keyword = keyword;
    }

    public static List<EmailKeywordEntity> of(Email email) {
        final Map<String, Boolean> keywords = email.getKeywords();
        if (keywords == null) {
            return Collections.emptyList();
        }
        final ImmutableList.Builder<EmailKeywordEntity> builder = new ImmutableList.Builder<>();
        for(String keyword : keywords.keySet()) {
            builder.add(new EmailKeywordEntity(email.getId(),keyword));
        }
        return builder.build();
    }

}
