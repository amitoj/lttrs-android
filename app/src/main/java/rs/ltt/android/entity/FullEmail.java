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

import android.util.Log;
import android.widget.ImageView;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    @Relation(parentColumn = "id", entityColumn = "emailId")
    public List<EmailBodyPartEntity> bodyPartEntities;

    @Relation(parentColumn = "id", entityColumn = "emailId")
    public List<EmailBodyValueEntity> bodyValueEntities;

    public String getFromAsText() {
        return getFrom().getKey();
    }

    public String getPreview() {
        return preview;
    }

    public Map.Entry<String, String> getFrom() {
        for (EmailAddress emailAddress : emailAddresses) {
            if (emailAddress.type == EmailAddressType.FROM) {
                return Maps.immutableEntry(emailAddress.getName(), emailAddress.getEmail());
            }
        }
        return null;
    }

    @BindingAdapter("from")
    public static void setFrom(final ImageView imageView, final Map.Entry<String, String> from) {
        if (from == null) {
            imageView.setImageDrawable(new AvatarDrawable(null, null));
        } else {
            imageView.setImageDrawable(new AvatarDrawable(from.getKey(), from.getValue()));
        }
    }

    public String getText() {
        final ArrayList<EmailBodyPartEntity> textBody = new ArrayList<>();
        for(EmailBodyPartEntity entity : bodyPartEntities) {
            if (entity.bodyPartType == EmailBodyPartType.TEXT_BODY) {
                textBody.add(entity);
            }
        }
        Log.d("lttrs","found "+textBody.size()+" text bodies");
        Collections.sort(textBody, (o1, o2) -> o1.position - o2.position);
        EmailBodyPartEntity first = Iterables.getFirst(textBody, null);
        Map<String,EmailBodyValueEntity> map = Maps.uniqueIndex(bodyValueEntities,value->value.partId);
        EmailBodyValueEntity value = map.get(first.partId);
        return value.value;
    }

}
