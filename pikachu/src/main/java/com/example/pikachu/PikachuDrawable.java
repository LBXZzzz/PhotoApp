package com.example.pikachu;

import static android.graphics.Color.WHITE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;

public class PikachuDrawable extends BitmapDrawable {
    // Only accessed from main thread.
    private static final Paint DEBUG_PAINT = new Paint();
    private static final float FADE_DURATION = 200f; //ms

    /**
     * Create or update the drawable on the target {@link ImageView} to display the supplied bitmap
     * image.
     */
    static void setBitmap(ImageView target, Context context, Bitmap bitmap,
                          Pikachu.LoadedFrom loadedFrom, boolean noFade, boolean debugging) {
        Drawable placeholder = target.getDrawable();
        if (placeholder instanceof AnimationDrawable) {
            ((AnimationDrawable) placeholder).stop();
        }
        PikachuDrawable drawable =
                new PikachuDrawable(context, bitmap, placeholder, loadedFrom, noFade, debugging);
        target.setImageDrawable(drawable);
    }

    /**
     * Create or update the drawable on the target {@link ImageView} to display the supplied
     * placeholder image.
     */
    static void setPlaceholder(ImageView target, Drawable placeholderDrawable) {
        target.setImageDrawable(placeholderDrawable);
        if (target.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) target.getDrawable()).start();
        }
    }

    private final boolean debugging;
    private final float density;
    private final Pikachu.LoadedFrom loadedFrom;

    Drawable placeholder;

    long startTimeMillis;
    boolean animating;
    int alpha = 0xFF;

    PikachuDrawable(Context context, Bitmap bitmap, Drawable placeholder,
                    Pikachu.LoadedFrom loadedFrom, boolean noFade, boolean debugging) {
        super(context.getResources(), bitmap);

        this.debugging = debugging;
        this.density = context.getResources().getDisplayMetrics().density;

        this.loadedFrom = loadedFrom;

        boolean fade = loadedFrom != Pikachu.LoadedFrom.MEMORY && !noFade;
        if (fade) {
            this.placeholder = placeholder;
            animating = true;
            startTimeMillis = SystemClock.uptimeMillis();
        }
    }

    @Override public void draw(Canvas canvas) {
        if (!animating) {
            super.draw(canvas);
        } else {
            float normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION;
            if (normalized >= 1f) {
                animating = false;
                placeholder = null;
                super.draw(canvas);
            } else {
                if (placeholder != null) {
                    placeholder.draw(canvas);
                }

                int partialAlpha = (int) (alpha * normalized);
                super.setAlpha(partialAlpha);
                super.draw(canvas);
                super.setAlpha(alpha);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    invalidateSelf();
                }
            }
        }

        if (debugging) {
            drawDebugIndicator(canvas);
        }
    }

    @Override public void setAlpha(int alpha) {
        this.alpha = alpha;
        if (placeholder != null) {
            placeholder.setAlpha(alpha);
        }
        super.setAlpha(alpha);
    }

    @Override public void setColorFilter(ColorFilter cf) {
        if (placeholder != null) {
            placeholder.setColorFilter(cf);
        }
        super.setColorFilter(cf);
    }

    @Override protected void onBoundsChange(Rect bounds) {
        if (placeholder != null) {
            placeholder.setBounds(bounds);
        }
        super.onBoundsChange(bounds);
    }

    private void drawDebugIndicator(Canvas canvas) {
        DEBUG_PAINT.setColor(WHITE);
        Path path = getTrianglePath(new Point(0, 0), (int) (16 * density));
        canvas.drawPath(path, DEBUG_PAINT);

        DEBUG_PAINT.setColor(loadedFrom.debugColor);
        path = getTrianglePath(new Point(0, 0), (int) (15 * density));
        canvas.drawPath(path, DEBUG_PAINT);
    }

    private static Path getTrianglePath(Point p1, int width) {
        Point p2 = new Point(p1.x + width, p1.y);
        Point p3 = new Point(p1.x, p1.y + width);

        Path path = new Path();
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);

        return path;
    }
}
