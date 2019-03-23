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

import com.google.common.collect.ImmutableList;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import rs.ltt.jmap.common.entity.Email;
import rs.ltt.jmap.common.entity.EmailBodyPart;

@Entity(tableName = "email_body_part",
        primaryKeys = {"emailId", "bodyPartType", "position"},
        foreignKeys = @ForeignKey(entity = EmailEntity.class,
                parentColumns = {"id"},
                childColumns = {"emailId"}
        )
)
public class EmailBodyPartEntity {

    @NonNull
    public String emailId;
    @NonNull
    public EmailBodyPartType bodyPartType;
    @NonNull
    public Integer position;
    public String partId;
    public String blobId;
    public Integer size;
    public String name;
    public String type;
    public String charset;
    public String disposition;
    public String cid;

    public static List<EmailBodyPartEntity> of(Email email) {
        final ImmutableList.Builder<EmailBodyPartEntity> builder = new ImmutableList.Builder<>();
        final List<EmailBodyPart> textBody = email.getTextBody();
        for (int i = 0; i < textBody.size(); ++i) {
            builder.add(of(email.getId(), EmailBodyPartType.TEXT_BODY, i, textBody.get(i)));
        }
        final List<EmailBodyPart> attachment = email.getAttachments();
        for (int i = 0; i < attachment.size(); ++i) {
            builder.add(of(email.getId(), EmailBodyPartType.ATTACHMENT, i, attachment.get(i)));
        }
        return builder.build();
    }

    private static EmailBodyPartEntity of(String emailId, EmailBodyPartType type, int position, EmailBodyPart emailBodyPart) {
        final EmailBodyPartEntity entity = new EmailBodyPartEntity();
        entity.emailId = emailId;
        entity.bodyPartType = type;
        entity.position = position;
        entity.partId = emailBodyPart.getPartId();
        entity.blobId = emailBodyPart.getBlobId();
        entity.size = emailBodyPart.getSize();
        entity.name = emailBodyPart.getName();
        entity.type = emailBodyPart.getType();
        entity.charset = emailBodyPart.getCharset();
        entity.disposition = emailBodyPart.getDisposition();
        entity.cid = emailBodyPart.getCid();
        return entity;
    }
}
