package com.harlie.xyzreader.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class ImageLoaderHelper {
    private final static String TAG = "LEE: <" + ImageLoaderHelper.class.getSimpleName() + ">";

    private static ImageLoaderHelper sInstance;

    public static ImageLoaderHelper getInstance(Context context) {
        Log.v(TAG, "getInstance");
        if (sInstance == null) {
            sInstance = new ImageLoaderHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    private final LruCache<String, Bitmap> mImageCache = new LruCache<>(20);
    private final ImageLoader mImageLoader;

    private ImageLoaderHelper(Context applicationContext) {
        Log.v(TAG, "ImageLoaderHelper");
        RequestQueue queue = Volley.newRequestQueue(applicationContext);
        ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {
            @Override
            public void putBitmap(String key, Bitmap value) {
                mImageCache.put(key, value);
            }

            @Override
            public Bitmap getBitmap(String key) {
                return mImageCache.get(key);
            }
        };
        mImageLoader = new ImageLoader(queue, imageCache);
    }

    public ImageLoader getImageLoader() {
        Log.v(TAG, "getImageLoader");
        return mImageLoader;
    }
}
