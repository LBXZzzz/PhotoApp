

package com.example.photoapp.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.widght.PhotoImageView;

import java.util.ArrayList;

public class PhotoPaperAdapter extends PagerAdapter {
    private ArrayList<PhotoImageView> photoImageViews;

    public PhotoPaperAdapter(ArrayList<PhotoImageView> photoImageViews){
        this.photoImageViews=photoImageViews;
    }
    @Override
    public int getCount() {
        return photoImageViews.size();
    }
    @Override
    public boolean isViewFromObject(@NonNull View arg0, @NonNull Object arg1) {
        return  arg0 == arg1;
    }
    @Override
    public void destroyItem(ViewGroup view, int position, @NonNull Object object) {
        view.removeView(photoImageViews.get(position));
    }
    @Override
    @NonNull
    public Object instantiateItem(ViewGroup view, int position) {
        view.addView(photoImageViews.get(position));
        return photoImageViews.get(position);
    }

}