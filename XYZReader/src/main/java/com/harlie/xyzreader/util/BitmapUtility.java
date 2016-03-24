package com.harlie.xyzreader.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


class BitmapUtilityException extends Exception {
    @SuppressWarnings("unused")
    public BitmapUtilityException(String detailMessage) {
        super(detailMessage);
    }
}


// from http://stackoverflow.com/questions/11790104/how-to-storebitmap-image-and-retrieve-image-from-sqlite-database-in-android
public class BitmapUtility {
    private final static String TAG = "LEE: <" + BitmapUtility.class.getSimpleName() + ">";

    // convert from bitmap to byte array
    @SuppressWarnings("WeakerAccess")
    public static byte[] getBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int quality = 0;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, stream);
        return stream.toByteArray();
    }

    // convert from bitmap to byte array - but only if larger than minImageSize
    @SuppressWarnings("unused")
    public static byte[] getBytes(Bitmap theBitmap, int minImageSize) {
        if (theBitmap == null || theBitmap.getWidth() <= minImageSize || theBitmap.getHeight() <= minImageSize || theBitmap.getByteCount() <= minImageSize) {
            Log.v(TAG, "getBytes: ==> FAIL because of INVALID IMAGE!");
            return null;
        }
        @SuppressWarnings("UnnecessaryLocalVariable") byte[] image = BitmapUtility.getBytes(theBitmap);
        return image;
    }

    // convert from byte array to bitmap
    @SuppressWarnings("unused")
    public static Bitmap getImage(byte[] image) {
        if (image == null || image.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    @SuppressWarnings("WeakerAccess")
    public static String getPathForSharedStorage() {
        @SuppressWarnings("UnnecessaryLocalVariable") String storage = Environment.getExternalStorageDirectory().toString();
        //String storage = "/storage/emulated/0/"; // this is what my device is seeing for above call
        //String storage = "/sdcard"; // because gmail can't read it, as the file must be in a 'sharable' location
        return storage;
    }

    @SuppressWarnings("WeakerAccess")
    public static String getPathForFile(final String filename) {
        return (getPathForSharedStorage() + File.separator + filename);
    }

    public static Uri getUriForFile(final String filename) {
        String fullpath = getPathForFile(filename);
        Uri theBitmapUri = Uri.parse("file://" + fullpath);
        Log.v(TAG, "getBitmapUriForFile: theBitmapUri=" + theBitmapUri);
        return theBitmapUri;
    }

    // instead of placing the Bitmap into a Parcelable.. (no room) we save to disk and load it back
    @SuppressWarnings("UnusedReturnValue")
    public static Uri saveOneBitmapToFlash(final Bitmap poster, @SuppressWarnings("SameParameterValue") final String filename) {
        Log.v(TAG, "saveOneBitmapToFlash(final Bitmap poster, final String filename=" + filename + ")");
        Uri theBitmapSaveUri = null;
        if (poster != null && filename != null) {
            theBitmapSaveUri = getUriForFile(filename);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, "saveOneBitmapToFlash: save image to: " + filename);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    poster.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    File f = new File(Environment.getExternalStorageDirectory() + File.separator + filename);
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        f.createNewFile();
                        FileOutputStream fo = new FileOutputStream(f);
                        fo.write(bytes.toByteArray());
                        bytes.close();
                    } catch (IOException e) {
                        Log.e(TAG, "saveOneBitmapToFlash: problem saving file: " + filename);
                    }
/*
                    // JUST FOR TESTING: to make sure we can read it back..
                    Bitmap verify = null;
                    try {
                        verify = BitmapUtility.readOneBitmapFromFlash(filename, poster.getWidth(), poster.getHeight());
                    } catch (BitmapUtilityException e) {
                        Log.e(TAG, "saveOneBitmapToFlash: problem verifying save for " + filename + ", error="+e);
                    }
                    Log.v(TAG, "saveOneBitmapToFlash: verify " + ((verify != null) ? "success!" : "failed."));
*/
                }
            }).start();

        }
        return theBitmapSaveUri;
    }

    /*
     * From: http://stackoverflow.com/questions/16804404/create-a-bitmap-drawable-from-file-path
     */
    @SuppressWarnings("unused")
    public static Bitmap readOneBitmapFromFlash(final String filename, int width, int height) throws BitmapUtilityException {
        Log.v(TAG, "readOneBitmapFromFlash(final String filename=" + filename + ", int width=" + width + ", int height=" + height + ")");
        Bitmap bitmap = null;
        if (filename != null) {
            String sdcard = getPathForSharedStorage();
            try {
                Log.v(TAG, "readOneBitmapFromFlash: read image from: " + filename);
                File image = new File(sdcard, filename);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                if (width != 0 && height != 0) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                    Log.v(TAG, "readOneBitmapFromFlash: READ OK! " + filename);
                } else {
                    Log.v(TAG, "readOneBitmapFromFlash: READ FAILED! " + filename);
                }
            } catch (Exception e) {
                throw new BitmapUtilityException("readOneBitmapFromFlash: problem reading Bitmap: " + sdcard + "/" + filename + ", error=" + e);
            }
        }
        if (bitmap != null) {
            Log.v(TAG, "readOneBitmapFromFlash: ok, width=" + bitmap.getWidth() + ", height=" + bitmap.getHeight());
        }
        return bitmap;
    }

    // from: http://stackoverflow.com/questions/3035692/how-to-convert-a-drawable-to-a-bitmap
    @SuppressWarnings("unused")
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
