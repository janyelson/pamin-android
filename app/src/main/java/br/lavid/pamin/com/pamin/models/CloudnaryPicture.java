package br.lavid.pamin.com.pamin.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import br.lavid.pamin.com.pamin.utils.CloudnaryPamim;
import br.lavid.pamin.com.pamin.utils.PictureTransformation;

/**
 * Created by araujojordan on 13/06/15.
 * Have a image reference to the Cloudnary with the URL
 * <p/>
 * The image name is in the follow format: USERNAMEID_1442402640.webp
 * Where the USERNAME is the name of the uploader and the number is the EPOCH time of the upload
 */
public class CloudnaryPicture {

    private String completeUrl, uri;
    private Bitmap picture;
    private PictureDownloadCallback pictureDownloadCallback;
    private SendPictureCallback sentCallback;

    /**
     * Create a CloudnaryPicture with the Url of the picture (saved in the DB or in the Cloudnary server)
     *
     * @param completeUrl The complete url (with HTTPS) of the picture on Cloudnary server)
     */
    public CloudnaryPicture(String completeUrl) {

        setCompleteUrl(completeUrl);
    }

    /**
     * Create a new CloudnaryPicture, sending the picture to the Cloudnary server
     *
     * @param picureToSave picture to be saved
     * @param user         uploader
     */
    public CloudnaryPicture(CloudnaryPamim cloudnaryInstance, Bitmap picureToSave, User user, SendPictureCallback callback) {
        Log.v("CloudPic", "Saving picture");
        this.picture = PictureTransformation.scaleDown(PictureTransformation.getCroppedBitmap(picureToSave), 300);
        this.sentCallback = callback;

        String filename = user.getName() + user.getId();
        filename += "_" + new Date().getTime();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        this.picture.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

        cloudnaryInstance.sendImage(filename, bs, new CloudnaryPamim.UploadPicCallBack() {
            @Override
            public void uploadCallback(String url, Boolean successful) {
                completeUrl = url;
                Log.v("CloudPic", "Picture upload complete: " + url);
                sentCallback.sentPicture(picture, url);
            }
        });
    }

    public String getCompleteUrl() {
        return completeUrl;
    }

    /**
     * Set URL of the picture
     *
     * @param completeUrl need to be on cloudnary server
     */
    private void setCompleteUrl(String completeUrl) {
        Log.v("CloudPic", "setCompleteUrl:" + completeUrl);
        if (completeUrl == null)
            throw new IllegalArgumentException("PamimPicture completeUrl is null");

        this.completeUrl = completeUrl;
    }

    public String getFilename() {
        return completeUrl.substring(completeUrl.lastIndexOf('/') + 1, completeUrl.length());
    }

    public String getUploader() {
        return completeUrl.substring(completeUrl.lastIndexOf('/') + 1, completeUrl.length()).substring(0, getFilename().lastIndexOf('_') - 1);
    }

    public void loadPicture(final Context ctx, CloudnaryPamim cloudnaryInstance, PictureDownloadCallback callback) {
        pictureDownloadCallback = callback;

        File localPicture = new File(ctx.getExternalCacheDir() + "/" + getFilename());
        if (localPicture.exists()) { //LOAD FROM LOCAL MEMORY
            Log.v("CloudPic", "loadPic, LOAD FROM LOCAL MEMORY");
            try {
                Uri imageUri = getFileUri(ctx, localPicture);
                picture = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), imageUri);
                pictureDownloadCallback.pictureComplete(picture, completeUrl);
                uri = getFileUri(ctx, localPicture).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else { //LOAD FROM URL
            Log.v("CloudPic", "loadPic, LOAD FROM URL");
            cloudnaryInstance.getImage(getFilename(), 300, 300, new CloudnaryPamim.DownloadPicCallback() {
                @Override
                public void downloadPicCallback(Bitmap downloadPicture) {

                    //SAVING PICTURE
                    String filename = getFilename();
                    File sd = ctx.getExternalCacheDir();
                    File dest = new File(sd, filename);

                    try {
                        FileOutputStream out = new FileOutputStream(dest);
                        //WEBP para PNG
                        downloadPicture.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                        uri = getFileUri(ctx, dest).toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //SAVE COMPLETE
                    pictureDownloadCallback.pictureComplete(downloadPicture, completeUrl);
                }
            });
        }
    }

    public void loadPicture(final Context ctx, CloudnaryPamim cloudnaryInstance, String url, PictureDownloadCallback callback) {
        pictureDownloadCallback = callback;

        File localPicture = new File(ctx.getExternalCacheDir() + "/" + getFilename());
        if (localPicture.exists()) { //LOAD FROM LOCAL MEMORY
            Log.v("CloudPic", "loadPic, LOAD FROM LOCAL MEMORY");
            try {
                Uri imageUri = getFileUri(ctx, localPicture);
                picture = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), imageUri);
                pictureDownloadCallback.pictureComplete(picture, completeUrl);
                uri = getFileUri(ctx, localPicture).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else { //LOAD FROM URL
            Log.v("CloudPic", "loadPic, LOAD FROM URL");
            cloudnaryInstance.getImage(url, getFilename(), 300, 300, new CloudnaryPamim.DownloadPicCallback() {
                @Override
                public void downloadPicCallback(Bitmap downloadPicture) {

                    //SAVING PICTURE
                    String filename = getFilename();
                    File sd = ctx.getExternalCacheDir();
                    File dest = new File(sd, filename);

                    try {
                        FileOutputStream out = new FileOutputStream(dest);
                        //WEBP para PNG
                        downloadPicture.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                        uri = getFileUri(ctx, dest).toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //SAVE COMPLETE
                    pictureDownloadCallback.pictureComplete(downloadPicture, completeUrl);
                }
            });
        }
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

    public interface PictureDownloadCallback {
        void pictureComplete(Bitmap downloadPicture, String url);
    }

    public interface SendPictureCallback {
        void sentPicture(Bitmap downloadPicture, String url);
    }
}
