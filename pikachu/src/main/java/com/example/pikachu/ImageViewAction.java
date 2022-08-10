package com.example.pikachu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class ImageViewAction extends Action<ImageView>{
    Callback callback;

    ImageViewAction(Pikachu pikachu, ImageView imageView, Request data, int memoryPolicy,
                    int networkPolicy, int errorResId, Drawable errorDrawable, String key, Object tag,
                    Callback callback, boolean noFade) {
        super(pikachu, imageView, data, memoryPolicy, networkPolicy, errorResId, errorDrawable, key,
                tag, noFade);
        this.callback = callback;
    }

    @Override public void complete(Bitmap result, Pikachu.LoadedFrom from) {
        if (result == null) {
            throw new AssertionError(
                    String.format("Attempted to complete action with no result!\n%s", this));
        }

        ImageView target = this.target.get();
        if (target == null) {
            return;
        }

        Context context =pikachu.context;
        boolean indicatorsEnabled = pikachu.indicatorsEnabled;
        PikachuDrawable.setBitmap(target, context, result, from, noFade, indicatorsEnabled);

        if (callback != null) {
            callback.onSuccess();
        }
    }

    @Override public void error() {
        ImageView target = this.target.get();
        if (target == null) {
            return;
        }
        if (errorResId != 0) {
            target.setImageResource(errorResId);
        } else if (errorDrawable != null) {
            target.setImageDrawable(errorDrawable);
        }

        if (callback != null) {
            callback.onError();
        }
    }

    @Override void cancel() {
        super.cancel();
        if (callback != null) {
            callback = null;
        }
    }
}
