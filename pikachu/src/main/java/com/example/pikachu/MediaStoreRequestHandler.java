package com.example.pikachu;

import static android.content.ContentUris.parseId;
import static android.provider.MediaStore.Images.Thumbnails.FULL_SCREEN_KIND;
import static android.provider.MediaStore.Images.Thumbnails.MICRO_KIND;
import static android.provider.MediaStore.Images.Thumbnails.MINI_KIND;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

public class MediaStoreRequestHandler extends ContentStreamRequestHandler {
    MediaStoreRequestHandler(Context context) {
        super(context);
    }

    private static final String[] CONTENT_ORIENTATION = new String[]{
            MediaStore.Images.ImageColumns.ORIENTATION
    };

    @Override
    public boolean canHandleRequest(Request data) {
        return super.canHandleRequest(data);
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        int exifOrientation = getExifOrientation(contentResolver, request.uri);

        String mimeType = contentResolver.getType(request.uri);
        boolean isVideo = mimeType != null && mimeType.startsWith("video/");

        if (request.hasSize()) {
            PikachuKind picassoKind = getPicassoKind(request.targetWidth, request.targetHeight);
            if (!isVideo && picassoKind == PikachuKind.FULL) {
                return new Result(null, getInputStream(request), Pikachu.LoadedFrom.DISK, exifOrientation);
            }

            long id = parseId(request.uri);

            BitmapFactory.Options options = createBitmapOptions(request);
            options.inJustDecodeBounds = true;

            calculateInSampleSize(request.targetWidth, request.targetHeight, picassoKind.width,
                    picassoKind.height, options, request);

            Bitmap bitmap;

            if (isVideo) {
                // Since MediaStore doesn't provide the full screen kind thumbnail, we use the mini kind
                // instead which is the largest thumbnail size can be fetched from MediaStore.
                int kind = (picassoKind == PikachuKind.FULL) ? MediaStore.Video.Thumbnails.MINI_KIND : picassoKind.androidKind;
                bitmap = MediaStore.Video.Thumbnails.getThumbnail(contentResolver, id, kind, options);
            } else {
                bitmap =
                        MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, picassoKind.androidKind, options);
            }

            if (bitmap != null) {
                return new Result(bitmap, null, Pikachu.LoadedFrom.DISK, exifOrientation);
            }
        }

        return new Result(null, getInputStream(request), Pikachu.LoadedFrom.DISK, exifOrientation);
    }

    static int getExifOrientation(ContentResolver contentResolver, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, CONTENT_ORIENTATION, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return 0;
            }
            return cursor.getInt(0);
        } catch (RuntimeException ignored) {
            // If the orientation column doesn't exist, assume no rotation.
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    static PikachuKind getPicassoKind(int targetWidth, int targetHeight) {
        if (targetWidth <= PikachuKind.MICRO.width && targetHeight <= PikachuKind.MICRO.height) {
            return PikachuKind.MICRO;
        } else if (targetWidth <= PikachuKind.MINI.width && targetHeight <= PikachuKind.MINI.height) {
            return PikachuKind.MINI;
        }
        return PikachuKind.FULL;
    }

    enum PikachuKind {
        MICRO(MICRO_KIND, 96, 96),
        MINI(MINI_KIND, 512, 384),
        FULL(FULL_SCREEN_KIND, -1, -1);

        final int androidKind;
        final int width;
        final int height;

        PikachuKind(int androidKind, int width, int height) {
            this.androidKind = androidKind;
            this.width = width;
            this.height = height;
        }
    }
}
