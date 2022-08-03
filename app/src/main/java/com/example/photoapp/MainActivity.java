package com.example.photoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG="MainActivity";
    static ArrayList<FileImgBean> fileImgBeans;
    static ArrayList<String> filePath=new ArrayList<>();
    static ArrayList<Uri> filePath2=new ArrayList<>();
    ImageView imageView;
    /**
     * 用于展示照片墙的GridView
     */
    private GridView mPhotoWall;

    /**
     * GridView的适配器
     */
    private PhotoWallAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        fileImgBeans=MainActivity.getImgList(this);
//        Log.d("zwy","FileImgBean"+fileImgBeans.get(0).getFilePath());
       /* ImageView imageView=findViewById(R.id.iv1);
        imageView.setImageResource(R.drawable.dog_20220803125240);*/
       /* mPhotoWall = findViewById(R.id.photo_wall);
        adapter = new PhotoWallAdapter(this, 0, filePath, mPhotoWall);
        mPhotoWall.setAdapter(adapter);*/

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
                Log.d(TAG,String.valueOf(id));
                Uri uri= Uri.withAppendedPath(mUri, "" + id );
                filePath2.add(uri);
                Cursor thumbCursor = context.getContentResolver().query(
                        MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                        thumbColumns, MediaStore.Images.Thumbnails.IMAGE_ID
                                + "=" + id, null, null);
                if (thumbCursor.moveToFirst()) {
                    info.setThumbPath(thumbCursor.getString(thumbCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA)));
                }
                info.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media
                        .DATA)));
                MainActivity.filePath.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media
                        .DATA)));
                info.setMimeType(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)));
                info.setTitle(cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)));
                info.setTakeTime(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images
                        .Media.DATE_TAKEN)));
                int columnIndexOrThrow = cursor.getColumnIndex(MediaStore.Images
                        .Media.SIZE);
                int anInt = cursor.getInt(columnIndexOrThrow);
                info.setImgSize(anInt + "");
                ImagesList.add(info);
            } while (cursor.moveToNext());
        }
        return ImagesList;
    }


}