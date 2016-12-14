package br.lavid.pamin.com.pamin.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Jordan on 16/09/2015.
 */
public class DevicePicture {

    private File file;
    private String filename;
    private Uri uri;
    private Bitmap picture;

    public DevicePicture(String filename, Context ctx) {
        this.filename = filename;
        file = new File(ctx.getExternalCacheDir() + "/" + filename + ".webp");
        Log.v("DevicePicture", file.getAbsolutePath());
    }

    public DevicePicture(Bitmap bitmap, String filename, Context ctx) {
        this.filename = filename;
        File sd = ctx.getExternalCacheDir();
        file = new File(sd, filename);

        file = new File(ctx.getExternalCacheDir() + "/" + filename + ".webp");
        if (file.exists()) {
            Log.e("DevicePicture", "File already exist");
            return;
        }
        Log.v("DevicePicture", file.getAbsolutePath());

        //SAVING PICTURE
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Bitmap getPicture(Context ctx) {
        if (picture != null) { //LOAD FROM RAM
            Log.v("DevicePicture", "Found on DevicePic");
            return picture;
        }
        if (file.exists()) { //LOAD FROM LOCAL MEMORY
            Log.v("DevicePicture", "Loading from SD Card");
            try {
                uri = getFileUri(ctx, file);
                picture = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), uri);
                Log.v("DevPic", "Picture captured, " + picture.getWidth() + "x" + picture.getHeight());
                return picture;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getFileName() {
        return filename;
    }

    private Uri getFileUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public Uri getUri(Context ctx) {
        return getFileUri(ctx, file);
    }
}
