package com.example.photoapp.widght;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class PhotoViewPaper extends ViewPager {
    public PhotoViewPaper(@NonNull Context context) {
        super(context);
    }

    public PhotoViewPaper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
}
