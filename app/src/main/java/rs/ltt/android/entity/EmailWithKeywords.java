package rs.ltt.android.entity;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;

import androidx.room.Relation;
import rs.ltt.jmap.common.entity.IdentifiableEmailWithKeywords;

public class EmailWithKeywords implements IdentifiableEmailWithKeywords {

    public String id;

    @Relation(entity = EmailKeywordEntity.class, parentColumn = "id", entityColumn = "emailId", projection = {"keyword"})
    public Set<String> keywords;

    @Override
    public Map<String, Boolean> getKeywords() {
        return Maps.asMap(keywords, keyword -> true);
    }

    @Override
    public String getId() {
        return this.id;
    }
}
