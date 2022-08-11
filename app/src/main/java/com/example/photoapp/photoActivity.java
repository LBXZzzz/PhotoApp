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

public class photoActivity extends AppCompatActivity {
    ArrayList<String> uriString=new ArrayList<>();
    ArrayList<Uri> uriList=new ArrayList<>();
    ViewPager vp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        uriString=getIntent().getStringArrayListExtra("selectUriStringList");
        for (int i = 0; i < uriString.size(); i++) {
            Log.d("zwy",uriString.get(i));
            uriList.add(Uri.parse(uriString.get(i)));
        }
        vp=findViewById(R.id.vp_photo);
        ArrayList<PhotoImageView> photoImageViews = new ArrayList<>();
        for (int i = 0; i < uriList.size(); i++) {
            PhotoImageView photoImageView = new PhotoImageView(getApplicationContext(),uriList.get(i));
            photoImageViews.add(photoImageView);
        }
        PhotoPaperAdapter photoPaperAdapter = new PhotoPaperAdapter(photoImageViews);
        vp.setAdapter(photoPaperAdapter);
    }
}