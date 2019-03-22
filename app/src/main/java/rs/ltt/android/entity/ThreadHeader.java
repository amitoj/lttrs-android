package rs.ltt.android.entity;

import android.util.Log;

import java.util.List;
import java.util.Set;

import androidx.room.Relation;
import rs.ltt.android.util.Keywords;
import rs.ltt.jmap.common.entity.Keyword;

public class ThreadHeader {

    public String subject;
    public String threadId;

    @Relation(parentColumn = "threadId", entityColumn = "threadId", entity = EmailEntity.class)
    public List<EmailWithKeywords> emailsWithKeywords;

    @Relation(parentColumn = "threadId", entityColumn = "threadId")
    public Set<KeywordOverwriteEntity> keywordOverwriteEntities;

    public boolean showAsFlagged() {

        Log.d("lttrs","showAsFlagged(). num overwrites: "+keywordOverwriteEntities.size());

        KeywordOverwriteEntity flaggedOverwrite = KeywordOverwriteEntity.getKeywordOverwrite(keywordOverwriteEntities, Keyword.FLAGGED);
        return flaggedOverwrite != null ? flaggedOverwrite.value : Keywords.anyHas(emailsWithKeywords, Keyword.FLAGGED);
    }


}
