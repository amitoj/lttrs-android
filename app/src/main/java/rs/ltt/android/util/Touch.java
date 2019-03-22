package rs.ltt.android.util;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;

public class Touch {

    public static void expandTouchArea(final View parent, final View view, final int dp) {
        float scale = parent.getContext().getResources().getDisplayMetrics().density;
        int padding = (int) (scale * dp);
        parent.post(() -> {
            Rect rect = new Rect();
            view.getHitRect(rect);
            rect.top -= padding;
            rect.left -= padding;
            rect.right += padding;
            rect.bottom += padding;
            parent.setTouchDelegate(new TouchDelegate(rect, view));
        });
    }
}
