package rs.ltt.android.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;

import rs.ltt.android.util.XEP0392Helper;

public class AvatarDrawable extends ColorDrawable {

    private final Paint paint;
    private final Paint textPaint;
    private String letter;

    public AvatarDrawable(String name, String key) {
        paint = new Paint();
        paint.setColor(key == null ? 0xff757575 : XEP0392Helper.rgbFromKey(key));
        paint.setAntiAlias(true);
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        this.letter = name == null ? null : String.valueOf(Character.toUpperCase(name.charAt(0)));
    }

    @Override
    public void draw(Canvas canvas) {
        float midx = getBounds().width() / 2.0f;
        float midy = getBounds().height() / 2.0f;
        float radius = Math.min(getBounds().width(), getBounds().height()) / 2.0f;
        textPaint.setTextSize(radius);
        Rect r = new Rect();
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();
        canvas.drawCircle(midx, midy, radius, paint);
        if (letter == null) {
            return;
        }
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.getTextBounds(letter, 0, letter.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        float y = cHeight / 2f + r.height() / 2f - r.bottom;
        canvas.drawText(letter, x, y, textPaint);
    }
}
