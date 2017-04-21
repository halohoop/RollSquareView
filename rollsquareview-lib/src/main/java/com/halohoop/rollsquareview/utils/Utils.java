package com.halohoop.rollsquareview.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Created by Pooholah on 2017/4/20.
 */

public class Utils {
    /**
     * 确保drawable是能够拿到宽高的drawable
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null
                && drawable.getIntrinsicWidth() > 0
                && drawable.getIntrinsicHeight() > 0) {
            return null;
        }
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        Log.i("Utilities",
                "drawableToBitmap drawable.getIntrinsicWidth()=" + drawable.getIntrinsicWidth()
                        + ",drawable.getIntrinsicHeight()="
                        + drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static int dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}
