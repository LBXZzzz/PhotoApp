package com.example.photoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.photoapp.adapter.PhotoPaperAdapter;
import com.example.widght.PhotoImageView;

import java.util.ArrayList;

public class PhotoActivity extends AppCompatActivity {
    private static final String TAG="PhotoActivity";
    ArrayList<String> uriString = new ArrayList<>();
    ArrayList<Uri> uriList = new ArrayList<>();
    ViewPager vp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        uriString = getIntent().getStringArrayListExtra("selectUriStringList");
        for (int i = 0; i < uriString.size(); i++) {
            uriList.add(Uri.parse(uriString.get(i)));
        }
        vp = findViewById(R.id.vp_photo);
        ArrayList<PhotoImageView> photoImageViews = new ArrayList<>();
        for (int i = 0; i < uriList.size(); i++) {
            PhotoImageView photoImageView = new PhotoImageView(getApplicationContext(), uriList.get(i));
            photoImageViews.add(photoImageView);
        }
        PhotoPaperAdapter photoPaperAdapter = new PhotoPaperAdapter(photoImageViews);
        vp.setAdapter(photoPaperAdapter);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int lastValue = -1;
            boolean isLeft=true;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(positionOffset!=0){
                    isLeft=true;
                    if(lastValue>=positionOffsetPixels){
                        //右滑
                        isLeft=false;
                    }else {
                        isLeft=true;
                    }
                }
                lastValue=positionOffsetPixels;
            }

            @Override
            public void onPageSelected(int position) {
                lastValue=-1;
                if(isLeft){
                    Log.d(TAG,"左滑");
                    photoImageViews.get(position-1).initAgain();

                }else {
                    Log.d(TAG,"右滑");
                    photoImageViews.get(position+1).initAgain();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}