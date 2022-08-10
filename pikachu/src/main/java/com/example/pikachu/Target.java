package com.example.pikachu;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public interface Target {
    void onBitmapLoaded(Bitmap bitmap, Pikachu.LoadedFrom from);


    void onBitmapFailed(Drawable errorDrawable);


    void onPrepareLoad(Drawable placeHolderDrawable);
}
