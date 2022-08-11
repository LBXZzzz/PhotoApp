package com.example.photoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.example.photoapp.adapter.PhotoPaperAdapter;
import com.example.photoapp.adapter.PhotoRecyclerAdapter;
import com.example.photoapp.entries.FileImgBean;
import com.example.pikachu.Pikachu;
import com.example.widght.PhotoImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    static ArrayList<FileImgBean> fileImgBeans;
    static ArrayList<String> filePath = new ArrayList<>();
    static ArrayList<Uri> filePath2 = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileImgBeans=MainActivity.getImgList(this);
        RecyclerView recyclerView =findViewById(R.id.rv_photo);
        StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(
                3,
                StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        PhotoRecyclerAdapter photoRecyclerAdapter=new PhotoRecyclerAdapter(this,filePath2);
        recyclerView.setAdapter(photoRecyclerAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                // 查看源码可知State有三种状态：SCROLL_STATE_IDLE（静止）、SCROLL_STATE_DRAGGING（上升）、SCROLL_STATE_SETTLING（下落）
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { // 滚动静止时才加载图片资源，极大提升流畅度
                   photoRecyclerAdapter.setScrolling(false);
                    photoRecyclerAdapter.notifyDataSetChanged(); // notify调用后onBindViewHolder会响应调用
                } else{
                    photoRecyclerAdapter.setScrolling(true);
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        ArrayList<PhotoImageView> photoImageViews = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            PhotoImageView photoImageView = new PhotoImageView(getApplicationContext());
            photoImageViews.add(photoImageView);
        }
        PhotoPaperAdapter photoPaperAdapter = new PhotoPaperAdapter(photoImageViews);
        ViewPager viewPager = findViewById(R.id.vp);
        viewPager.setAdapter(photoPaperAdapter);
    }

    /**
     * 查找图片
     *
     * @param context
     * @return
     */
    public static ArrayList<FileImgBean> getImgList(Context context) {
        Uri mUri = Uri.parse("content://media/external/images/media");
        ArrayList<FileImgBean> ImagesList = new ArrayList<>();
        // MediaStore.Images.Thumbnails.DATA:视频缩略图的文件路径
        String[] thumbColumns = {MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Thumbnails._ID};
        // 视频其他信息的查询条件
        String[] mediaColumns = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.SIZE};

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media
                        .EXTERNAL_CONTENT_URI,
                mediaColumns, null, null, null);

        if (cursor == null) {
            return ImagesList;
        }
        if (cursor.moveToFirst()) {
            do {
                FileImgBean info = new FileImgBean();
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                Log.d(TAG, String.valueOf(id));
                Uri uri = Uri.withAppendedPath(mUri, "" + id);
                filePath2.add(uri);
            } while (cursor.moveToNext());
        }
        return ImagesList;
    }


}