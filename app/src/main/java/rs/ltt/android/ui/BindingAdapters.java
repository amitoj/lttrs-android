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

package rs.ltt.android.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.databinding.BindingAdapter;
import rs.ltt.android.R;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.android.util.EmailAddressUtil;
import rs.ltt.jmap.common.entity.Role;
import rs.ltt.jmap.mua.util.EmailBodyUtil;

public class BindingAdapters {

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

    @BindingAdapter("date")
    public static void setInteger(TextView textView, Date receivedAt) {
        if (receivedAt == null || receivedAt.getTime() <= 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            if (isToday(receivedAt)) { //TODO or less than 6 hours ago
                textView.setText(TIME_FORMAT.format(receivedAt));
            } else {
                textView.setText(DATE_FORMAT.format(receivedAt));
            }
        }
    }

    @BindingAdapter("body")
    public static void setBody(final TextView textView, String body) {
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        for (EmailBodyUtil.Block block : EmailBodyUtil.parse(body)) {
            if (builder.length() != 0) {
                builder.append('\n');
            }
            int start = builder.length();
            builder.append(block.toString());
            if (block.getDepth() > 0) {
                builder.setSpan(new QuoteSpan(block.getDepth(), textView.getContext()), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        textView.setText(builder);
    }

    @BindingAdapter("to")
    public static void setTo(final TextView textView, final Collection<String> names) {
        final boolean shorten = names.size() > 1;
        final StringBuilder builder = new StringBuilder();
        for (String name : names) {
            if (builder.length() != 0) {
                builder.append(", ");
            }
            builder.append(shorten ? EmailAddressUtil.shorten(name) : name);
        }
        final Context context = textView.getContext();
        textView.setText(context.getString(R.string.to_x, builder.toString()));
    }

    @BindingAdapter("from")
    public static void setFrom(final ImageView imageView, final Map.Entry<String, String> from) {
        if (from == null) {
            imageView.setImageDrawable(new AvatarDrawable(null, null));
        } else {
            imageView.setImageDrawable(new AvatarDrawable(from.getKey(), from.getValue()));
        }
    }

    @BindingAdapter("from")
    public static void setFrom(final TextView textView, final ThreadOverviewItem.From[] from) {
        final boolean shorten = from.length > 1;
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int i = 0; i < from.length; ++i) {
            ThreadOverviewItem.From individual = from[i];
            if (builder.length() != 0) {
                builder.append(", ");
            }
            int start = builder.length();
            builder.append(shorten ? EmailAddressUtil.shorten(individual.name) : individual.name);
            if (!individual.seen) {
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (from.length > 3) {
                if (i < from.length - 3) {
                    builder.append(" … "); //TODO small?
                    i = from.length - 3;
                }
            }
        }
        textView.setText(builder);
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
    public static void setThreadOverviewFrom(final ImageView imageView, final Map.Entry<String, ThreadOverviewItem.From> from) {
        if (from == null) {
            imageView.setImageDrawable(new AvatarDrawable(null, null));
        } else {
            imageView.setImageDrawable(new AvatarDrawable(from.getValue().name, from.getKey()));
        }
    }

    @BindingAdapter("count")
    public static void setInteger(TextView textView, Integer integer) {
        if (integer == null || integer <= 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.valueOf(integer));
        }
    }

    @BindingAdapter("role")
    public static void setRole(final ImageView imageView, final Role role) {
        @DrawableRes final int imageResource;
        if (role == null) {
            imageResource = R.drawable.ic_label_black_24dp;
        } else {
            switch (role) {
                case INBOX:
                    imageResource = R.drawable.ic_inbox_black_24dp;
                    break;
                case ARCHIVE:
                    imageResource = R.drawable.ic_archive_black_24dp;
                    break;
                case DRAFTS:
                    imageResource = R.drawable.ic_drafts_black_24dp;
                    break;
                case TRASH:
                    imageResource = R.drawable.ic_delete_black_24dp;
                    break;
                case SENT:
                    imageResource = R.drawable.ic_send_black_24dp;
                    break;
                default:
                    imageResource = R.drawable.ic_folder_black_24dp;
            }
        }
        imageView.setImageResource(imageResource);
    }
}
