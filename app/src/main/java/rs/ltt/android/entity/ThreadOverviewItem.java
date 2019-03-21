package rs.ltt.android.entity;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.databinding.BindingAdapter;
import androidx.room.Ignore;
import androidx.room.Relation;
import rs.ltt.android.R;
import rs.ltt.android.ui.AvatarDrawable;
import rs.ltt.jmap.common.entity.Keyword;

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
        final List<Email> emails = getOrderedEmails();
        for (Email email : emails) {
            if (!email.keywords.contains(Keyword.SEEN)) {
                return false;
            }
        }
        return true;
    }

    private KeywordOverwriteEntity getKeywordOverwrite(String keyword) {
        for(KeywordOverwriteEntity keywordOverwriteEntity : keywordOverwriteEntities) {
            if (keyword.equals(keywordOverwriteEntity.keyword)) {
                return keywordOverwriteEntity;
            }
        }
        return null;
    }

    public boolean showAsFlagged() {
        KeywordOverwriteEntity flaggedOverwrite = getKeywordOverwrite(Keyword.FLAGGED);
        return flaggedOverwrite != null ? flaggedOverwrite.value : isFlagged();
    }

    private boolean isFlagged() {
        final List<Email> emails = getOrderedEmails();
        for(Email email : emails) {
            if (email.keywords.contains(Keyword.FLAGGED)) {
                return true;
            }
        }
        return false;
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
        LinkedHashMap<String, From> fromMap = new LinkedHashMap<>();
        final List<Email> emails = getOrderedEmails();
        for (Email email : emails) {
            final boolean seen = email.keywords.contains(Keyword.SEEN);
            for (EmailAddress emailAddress : email.emailAddresses) {
                if (emailAddress.type == EmailAddressType.FROM) {
                    From from = fromMap.get(emailAddress.email);
                    if (from == null) {
                        final String name = emailAddress.name == null ? emailAddress.email.split("@")[0] : emailAddress.name;
                        from = new From(name, seen);
                        fromMap.put(emailAddress.email, from);
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

    public static class Email {

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

    @BindingAdapter("android:text")
    public static void setFroms(final TextView textView, final From[] froms) {
        final boolean shorten = froms.length > 1;
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int i = 0; i < froms.length; ++i) {
            From from = froms[i];
            if (builder.length() != 0) {
                builder.append(", ");
            }
            int start = builder.length();
            builder.append(shorten ? from.name.split("\\s")[0] : from.name);
            if (!from.seen) {
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (froms.length > 3) {
                if (i < froms.length - 3) {
                    builder.append(" … "); //TODO small?
                    i = froms.length - 3;
                }
            }
        }
        textView.setText(builder);
    }

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd");

    private static boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar specifiedDate = Calendar.getInstance();
        specifiedDate.setTime(date);

        return today.get(Calendar.DAY_OF_MONTH) == specifiedDate.get(Calendar.DAY_OF_MONTH)
                && today.get(Calendar.MONTH) == specifiedDate.get(Calendar.MONTH)
                && today.get(Calendar.YEAR) == specifiedDate.get(Calendar.YEAR);
    }

    @BindingAdapter("android:text")
    public static void setInteger(TextView textView, Date receivedAt) {
        if (receivedAt == null || receivedAt.getTime() <= 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            if (isToday(receivedAt)) {
                textView.setText(TIME_FORMAT.format(receivedAt));
            } else {
                textView.setText(DATE_FORMAT.format(receivedAt));
            }
        }
    }

    @BindingAdapter("android:typeface")
    public static void setTypeface(TextView v, String style) {
        switch (style) {
            case "bold":
                v.setTypeface(null, Typeface.BOLD);
                break;
            default:
                v.setTypeface(null, Typeface.NORMAL);
                break;
        }
    }

    @BindingAdapter("isFlagged")
    public static void setIsFlagged(final ImageView imageView, final boolean isFlagged) {
        if (isFlagged) {
            imageView.setImageResource(R.drawable.ic_star_black_24dp);
            ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(ContextCompat.getColor(imageView.getContext(), R.color.colorPrimary)));
        } else {
            imageView.setImageResource(R.drawable.ic_star_border_black_24dp);
            ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(ContextCompat.getColor(imageView.getContext(), R.color.black54)));
        }
    }

    @BindingAdapter("from")
    public static void setFrom(final ImageView imageView, final Map.Entry<String, ThreadOverviewItem.From> from) {
        if (from == null) {
            imageView.setImageDrawable(new AvatarDrawable(null,null));
        } else {
            imageView.setImageDrawable(new AvatarDrawable(from.getValue().name, from.getKey()));
        }
    }
}
