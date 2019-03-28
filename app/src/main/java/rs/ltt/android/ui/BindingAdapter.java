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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import rs.ltt.android.R;

public class BindingAdapter {

    @androidx.databinding.BindingAdapter("body")
    public static void setBody(final TextView textView, String body) {
        String[] lines = body.split("\\n");
        ArrayList<Block> blocks = new ArrayList<>();

        Block currentBlock = null;

        for (int i = 0; i < lines.length; ++i) {
            String currentLine = lines[i];

            QuoteIndicator quoteIndicator = QuoteIndicator.quoteDepth(currentLine);
            if (currentBlock == null) {
                currentBlock = new Block(quoteIndicator.depth);
                blocks.add(currentBlock);
            } else if (quoteIndicator.depth != currentBlock.depth) {
                currentBlock = new Block(quoteIndicator.depth);
                blocks.add(currentBlock);
            }

            String withQuoteRemoved;
            if (quoteIndicator.chars > 0) {
                withQuoteRemoved = currentLine.substring(quoteIndicator.chars);
            } else {
                withQuoteRemoved = currentLine;
            }

            currentBlock.append(withQuoteRemoved);

        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (Block block : blocks) {
            if (builder.length() != 0) {
                builder.append('\n');
            }
            int start = builder.length();
            builder.append(block.toString());
            if (block.depth > 0) {
                builder.setSpan(new QuoteSpan(block.depth, textView.getContext()), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        textView.setText(builder);
    }

    private static class Block {

        private final int depth;
        private final ArrayList<String> lines = new ArrayList<>();

        public Block(int depth) {
            this.depth = depth;
        }

        public void append(final String line) {
            this.lines.add(line);
        }

        private int maxLineLength() {
            int max = 0;
            for (String line : lines) {
                max = Math.max(line.length(), max);
            }
            return max;
        }

        public String toString() {
            int max = Math.min(70, maxLineLength());

            int lastLineLength = max;

            boolean breakNextLine = false;

            StringBuilder stringBuilder = new StringBuilder();
            for (String line : this.lines) {
                String[] words = line.split("\\s+");
                String firstWord = words.length == 0 ? "" : words[0];
                if (stringBuilder.length() != 0) {
                    if (breakNextLine) {
                        stringBuilder.append('\n');
                        lastLineLength = line.length();
                    } else if (depth > 0 && words.length == 1) {
                        stringBuilder.append(' ');
                        lastLineLength += line.length();
                    } else if (firstWord.endsWith(")") || firstWord.endsWith(":") || line.startsWith("* ") || line.startsWith("- ")) {
                        stringBuilder.append('\n');
                        lastLineLength = line.length();
                    } else if (lastLineLength + firstWord.length() < max) {
                        stringBuilder.append('\n');
                        lastLineLength = line.length();
                    } else {
                        stringBuilder.append(' ');
                        lastLineLength = line.length();
                    }
                }

                breakNextLine = (breakNextLine && !line.isEmpty()) || line.endsWith(":") || line.contains("__");

                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }

    }

    public static class QuoteIndicator {

        private final int chars;
        private final int depth;

        public QuoteIndicator(int chars, int depth) {
            this.chars = chars;
            this.depth = depth;
        }

        public static QuoteIndicator quoteDepth(String line) {
            int quoteDepth = 0;
            int chars = 0;
            for (int i = 0; i < line.length(); ++i) {
                char c = line.charAt(i);
                if (c == '>') {
                    quoteDepth++;
                } else if (c != ' ') {
                    break;
                }
                ++chars;
            }
            return new QuoteIndicator(chars, quoteDepth);
        }
    }


    @androidx.databinding.BindingAdapter("to")
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

    @androidx.databinding.BindingAdapter("from")
    public static void setFrom(final ImageView imageView, final Map.Entry<String, String> from) {
        if (from == null) {
            imageView.setImageDrawable(new AvatarDrawable(null, null));
        } else {
            imageView.setImageDrawable(new AvatarDrawable(from.getKey(), from.getValue()));
        }
    }
}
