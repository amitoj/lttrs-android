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

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Objects;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import rs.ltt.android.R;
import rs.ltt.jmap.common.entity.Role;

public class MailboxOverviewItem {

    @NonNull public String id;

    public String parentId;

    public String name;

    public Role role;

    public Integer sortOrder;

    public Integer totalThreads;

    public Integer unreadThreads;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailboxOverviewItem that = (MailboxOverviewItem) o;
        return Objects.equal(id, that.id) &&
                Objects.equal(parentId, that.parentId) &&
                Objects.equal(name, that.name) &&
                role == that.role &&
                Objects.equal(sortOrder, that.sortOrder) &&
                Objects.equal(totalThreads, that.totalThreads) &&
                Objects.equal(unreadThreads, that.unreadThreads);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, parentId, name, role, sortOrder, totalThreads, unreadThreads);
    }

    @BindingAdapter("android:text")
    public static void setInteger(TextView textView, Integer integer) {
        if (integer == null || integer <= 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.valueOf(integer));
        }
    }

    @BindingAdapter("android:src")
    public static void setRole(final ImageView imageView, final Role role) {
        @DrawableRes final int imageResource;
        if (role == null) {
            imageResource = R.drawable.ic_folder_black_24dp;
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
