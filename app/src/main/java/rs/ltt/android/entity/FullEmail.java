package rs.ltt.android.entity;

import android.widget.ImageView;

import com.google.common.collect.Maps;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.databinding.BindingAdapter;
import androidx.room.Relation;
import rs.ltt.android.ui.AvatarDrawable;

public class FullEmail {

    public String id;
    public String preview;
    public String threadId;
    public Date receivedAt;

    @Relation(entity = EmailKeywordEntity.class, parentColumn = "id", entityColumn = "emailId", projection = {"keyword"})
    public Set<String> keywords;

    @Relation(entity = EmailEmailAddressEntity.class, parentColumn = "id", entityColumn = "emailId", projection = {"email", "name", "type"})
    public List<EmailAddress> emailAddresses;

    public String getFromAsText() {
        return getFrom().getKey();
    }

    public String getPreview() {
        return preview;
    }

    public Map.Entry<String, String> getFrom() {
        for(EmailAddress emailAddress : emailAddresses) {
            if (emailAddress.type == EmailAddressType.FROM) {
                return Maps.immutableEntry(emailAddress.getName(), emailAddress.getEmail());
            }
        }
        return null;
    }

    @BindingAdapter("from")
    public static void setFrom(final ImageView imageView, final Map.Entry<String, String> from) {
        if (from == null) {
            imageView.setImageDrawable(new AvatarDrawable(null,null));
        } else {
            imageView.setImageDrawable(new AvatarDrawable(from.getKey(), from.getValue()));
        }
    }

}
