package com.example.pikachu;

import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class DeferredRequestCreator implements ViewTreeObserver.OnPreDrawListener {
    final RequestCreator creator;
    final WeakReference<ImageView> target;
    Callback callback;

    DeferredRequestCreator(RequestCreator creator, ImageView target) {
        this(creator, target, null);
    }

    DeferredRequestCreator(RequestCreator creator, ImageView target, Callback callback) {
        this.creator = creator;
        this.target = new WeakReference<>(target);
        this.callback = callback;
        target.getViewTreeObserver().addOnPreDrawListener(this);
    }

    @Override
    public boolean onPreDraw() {
        ImageView target = this.target.get();
        if (target == null) {
            return true;
        }
        ViewTreeObserver vto = target.getViewTreeObserver();
        if (!vto.isAlive()) {
            return true;
        }

        int width = target.getWidth();
        int height = target.getHeight();

        if (width <= 0 || height <= 0) {
            return true;
        }

        vto.removeOnPreDrawListener(this);

        this.creator.unfit().resize(width, height).into(target, callback);
        return true;
    }


    void cancel() {
        callback = null;
        ImageView target = this.target.get();
        if (target == null) {
            return;
        }
        ViewTreeObserver vto = target.getViewTreeObserver();
        if (!vto.isAlive()) {
            return;
        }
        vto.removeOnPreDrawListener(this);
    }
}
