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

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.databinding.BindingAdapter;
import androidx.room.Relation;
import rs.ltt.android.R;
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
        for (EmailBodyPartEntity entity : bodyPartEntities) {
            if (entity.bodyPartType == EmailBodyPartType.TEXT_BODY) {
                textBody.add(entity);
            }
        }
        Collections.sort(textBody, (o1, o2) -> o1.position - o2.position);
        EmailBodyPartEntity first = Iterables.getFirst(textBody, null);
        Map<String, EmailBodyValueEntity> map = Maps.uniqueIndex(bodyValueEntities, value -> value.partId);
        EmailBodyValueEntity value = map.get(first.partId);
        return value.value;
    }

    public Collection<String> getTo() {
        LinkedHashMap<String, String> toMap = new LinkedHashMap<>();
        for (EmailAddress emailAddress : emailAddresses) {
            if (emailAddress.type == EmailAddressType.TO || emailAddress.type == EmailAddressType.CC) {
                toMap.put(emailAddress.getEmail(), emailAddress.getName());
            }
        }
        return toMap.values();
    }

    @BindingAdapter("to")
    public static void setFroms(final TextView textView, final Collection<String> names) {
        final boolean shorten = names.size() > 1;
        StringBuilder builder = new StringBuilder();
        for (String name : names) {
            if (builder.length() != 0) {
                builder.append(", ");
            }
            final String shortened = name.split("")[0];
            builder.append(shorten && shortened.length() > 3 ? shortened : name);
        }
        final Context context = textView.getContext();
        textView.setText(context.getString(R.string.to_x, builder.toString()));
    }

}
