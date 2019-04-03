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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import androidx.room.Ignore;
import androidx.room.Relation;
import rs.ltt.jmap.common.entity.IdentifiableEmailWithKeywords;
import rs.ltt.jmap.common.entity.Keyword;
import rs.ltt.jmap.mua.util.KeywordUtil;

public class ThreadOverviewItem {

    @Ignore
    private final AtomicReference<Map<String, From>> fromMap = new AtomicReference<>();
    @Ignore
    private final AtomicReference<List<Email>> orderedEmails = new AtomicReference<>();

    public String emailId;
    public String threadId;

    @Relation(parentColumn = "threadId", entityColumn = "threadId", entity = EmailEntity.class)
    public List<Email> emails;

    @Relation(parentColumn = "threadId", entityColumn = "threadId")
    public List<ThreadItemEntity> threadItemEntities;

    @Relation(parentColumn = "threadId", entityColumn = "threadId")
    public Set<KeywordOverwriteEntity> keywordOverwriteEntities;


    public String getPreview() {
        final Email email = Iterables.getLast(getOrderedEmails(), null);
        return email == null ? "(no preview)" : email.preview;
    }

    public String getSubject() {
        final Email email = Iterables.getFirst(getOrderedEmails(), null);
        return email == null ? "(no subject)" : email.subject;
    }

    public Date getReceivedAt() {
        final Email email = Iterables.getLast(getOrderedEmails(), null);
        return email == null ? null : email.receivedAt;
    }

    public boolean everyHasSeenKeyword() {
        KeywordOverwriteEntity seenOverwrite = KeywordOverwriteEntity.getKeywordOverwrite(keywordOverwriteEntities, Keyword.SEEN);
        return seenOverwrite != null ? seenOverwrite.value : KeywordUtil.everyHas(getOrderedEmails(), Keyword.SEEN);
    }

    public boolean showAsFlagged() {
        KeywordOverwriteEntity flaggedOverwrite = KeywordOverwriteEntity.getKeywordOverwrite(keywordOverwriteEntities, Keyword.FLAGGED);
        return flaggedOverwrite != null ? flaggedOverwrite.value : KeywordUtil.anyHas(getOrderedEmails(),Keyword.FLAGGED);
    }


    public Integer getCount() {
        final int count = threadItemEntities.size();
        return count <= 1 ? null : count;
    }

    public Map.Entry<String, From> getFrom() {
        return Iterables.getFirst(getFromMap().entrySet(), null);
    }

    private Map<String, From> getFromMap() {
        Map<String, From> map = this.fromMap.get();
        if (map == null) {
            synchronized (this.fromMap) {
                map = this.fromMap.get();
                if (map == null) {
                    map = calculateFromMap();
                    this.fromMap.set(map);
                }
            }
        }
        return map;
    }

    private Map<String, From> calculateFromMap() {
        KeywordOverwriteEntity seenOverwrite = KeywordOverwriteEntity.getKeywordOverwrite(keywordOverwriteEntities, Keyword.SEEN);
        LinkedHashMap<String, From> fromMap = new LinkedHashMap<>();
        final List<Email> emails = getOrderedEmails();
        for (Email email : emails) {
            final boolean seen = seenOverwrite != null ? seenOverwrite.value : email.keywords.contains(Keyword.SEEN);
            for (EmailAddress emailAddress : email.emailAddresses) {
                if (emailAddress.type == EmailAddressType.FROM) {
                    From from = fromMap.get(emailAddress.getEmail());
                    if (from == null) {
                        from = new From(emailAddress.getName(), seen);
                        fromMap.put(emailAddress.getEmail(), from);
                    } else {
                        from.seen &= seen;
                    }
                }
            }
        }
        return fromMap;
    }

    private List<Email> getOrderedEmails() {
        List<Email> list = this.orderedEmails.get();
        if (list == null) {
            synchronized (this.orderedEmails) {
                list = this.orderedEmails.get();
                if (list == null) {
                    list = calculateOrderedEmails();
                    this.orderedEmails.set(list);
                }
            }
        }
        return list;
    }

    private List<Email> calculateOrderedEmails() {
        final List<ThreadItemEntity> threadItemEntities = new ArrayList<>(this.threadItemEntities);
        Collections.sort(threadItemEntities, (o1, o2) -> o1.getPosition() - o2.getPosition());
        final Map<String, Email> emailMap = Maps.uniqueIndex(emails, new Function<Email, String>() {
            @NullableDecl
            @Override
            public String apply(Email input) {
                return input.id;
            }
        });
        final List<Email> orderedList = new ArrayList<>(emails.size());
        for(ThreadItemEntity threadItemEntity : threadItemEntities) {
            Email email = emailMap.get(threadItemEntity.emailId);
            if (email != null) {
                orderedList.add(email);
            }
        }
        return orderedList;
    }

    public From[] getFromValues() {
        return getFromMap().values().toArray(new From[0]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreadOverviewItem item = (ThreadOverviewItem) o;
        return Objects.equal(getSubject(), item.getSubject()) &&
                Objects.equal(getPreview(), item.getPreview()) &&
                Objects.equal(showAsFlagged(), item.showAsFlagged()) &&
                Objects.equal(getReceivedAt(), item.getReceivedAt()) &&
                Objects.equal(everyHasSeenKeyword(), item.everyHasSeenKeyword()) &&
                Arrays.equals(getFromValues(),item.getFromValues());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(emailId, threadId, getOrderedEmails(), threadItemEntities);
    }

    public static class Email implements IdentifiableEmailWithKeywords {

        public String id;
        public String preview;
        public String threadId;
        public String subject;
        public Date receivedAt;

        @Relation(entity = EmailKeywordEntity.class, parentColumn = "id", entityColumn = "emailId", projection = {"keyword"})
        public Set<String> keywords;

        @Relation(entity = EmailEmailAddressEntity.class, parentColumn = "id", entityColumn = "emailId", projection = {"email", "name", "type"})
        public List<EmailAddress> emailAddresses;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Email email = (Email) o;
            return Objects.equal(id, email.id) &&
                    Objects.equal(preview, email.preview) &&
                    Objects.equal(threadId, email.threadId) &&
                    Objects.equal(subject, email.subject) &&
                    Objects.equal(receivedAt, email.receivedAt) &&
                    Objects.equal(keywords, email.keywords) &&
                    Objects.equal(emailAddresses, email.emailAddresses);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id, preview, threadId, subject, receivedAt, keywords, emailAddresses);
        }

        @Override
        public Map<String, Boolean> getKeywords() {
            return Maps.asMap(keywords, keyword -> true);
        }

        @Override
        public String getId() {
            return id;
        }
    }

    public static class From {
        public final String name;
        public boolean seen;

        From(String name, boolean seen) {
            this.name = name;
            this.seen = seen;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            From from = (From) o;
            return seen == from.seen &&
                    Objects.equal(name, from.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name, seen);
        }
    }
}
