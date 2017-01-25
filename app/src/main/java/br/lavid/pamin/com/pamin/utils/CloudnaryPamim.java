package br.lavid.pamin.com.pamin.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.android.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by araujojordan on 08/06/15.
 * Class to send and receive pictures from Cloudnary.
 * The Account of the server is on AndroidManifest metadata.
 */
public class CloudnaryPamim {

    private static String SENDING_ERROR = "errorOnSending";

    private Cloudinary cloudinary;
    private CloudnaryPicSender cloudnarySender;
    private String cloudUrl;
    private String fit = "c_fit,h_300,w_300/";

    /**
     * Constructor to use this class
     * After call it, use it for send and receive many times you want
     *
     * @param context some activity context
     */
    public CloudnaryPamim(Context context) {
        cloudinary = new Cloudinary(Utils.cloudinaryUrlFromContext(context));
    }

    /**
     * @param uniqueName The file name on the server (need to be unique)
     * @param picture    The InputStream of the picture (can be get with the Uri of the file)
     */
    public void sendImage(String uniqueName, InputStream picture, UploadPicCallBack uploadCallBack) {
        cloudnarySender = new CloudnaryPicSender();
        cloudnarySender.execute(new PictureWithMetadata(uniqueName, picture, uploadCallBack));
    }

    /**
     * Download Image from server with the given title
     *
     * @param fileName  title of the image
     * @param maxWidth  max size of width
     * @param maxHeight max size of height
     */
    public void getImage(String fileName, int maxWidth, int maxHeight, DownloadPicCallback downloadPicCallback) {
        Log.e("url antes: ", fileName);
        String url = cloudinary.url().transformation(new Transformation().width(maxWidth).height(maxHeight).crop("fit")).generate(fileName);
        Log.e("url depois: ", url);
        UrlWithCallback urlWithCallback = new UrlWithCallback(url, downloadPicCallback);
        new DownloadImageTask().execute(urlWithCallback);
    }

    public void getImage(String cloudUrl, String fileName, int maxWidth, int maxHeight, DownloadPicCallback downloadPicCallback) {
        this.cloudUrl = cloudUrl;
        String url = cloudinary.url().transformation(new Transformation().width(maxWidth).height(maxHeight).crop("fit")).generate(fileName);
        UrlWithCallback urlWithCallback = new UrlWithCallback(url, downloadPicCallback);
        new DownloadImageTask().execute(urlWithCallback);
    }


    /**
     * Upload Picture Callback to inform the Url string and if was successful
     * OBS: RUN ON ASYNC, call runOnUiThread() to run on it
     */
    public interface UploadPicCallBack {
        /**
         * @param url        link of the picture if was sucessful
         * @param successful true if image was sent, false if an error happen
         */
        void uploadCallback(String url, Boolean successful);
    }

    /**
     * Download Picture Callback to inform the image
     * OBS: RUN ON ASYNC, call runOnUiThread() to run on it
     */
    public interface DownloadPicCallback {
        void downloadPicCallback(Bitmap picture);
    }

    /**
     * Picture with a title
     */
    private class PictureWithMetadata {

        public UploadPicCallBack uploadCallBack;
        private String name;
        private InputStream stream;

        public PictureWithMetadata(String name, InputStream stream, UploadPicCallBack uploadCallBack) {
            this.name = name;
            this.stream = stream;
            this.uploadCallBack = uploadCallBack;
        }

        public InputStream getStream() {
            return stream;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Picture with a title
     */
    private class UrlWithCallback {

        public DownloadPicCallback downloadPicCallback;
        private String url;

        public UrlWithCallback(String url, DownloadPicCallback downloadPicCallback) {
            this.url = url;
            this.downloadPicCallback = downloadPicCallback;
        }

        public String getUrl() {
            return url;
        }

        public DownloadPicCallback getDownloadPicCallback() {
            return downloadPicCallback;
        }
    }

    private class CloudnaryPicSender extends AsyncTask<PictureWithMetadata, Void, Void> {

        @Override
        protected Void doInBackground(PictureWithMetadata... pic) {

            Map<String, String> options;

            for (PictureWithMetadata picture : pic) {
                options = new HashMap<>();
                options.put("public_id", picture.getName());
                try {
                    Map result = cloudinary.uploader().
                            upload(picture.getStream(), options);
                    picture.uploadCallBack.
                            uploadCallback((String) result.get("secure_url"), true);
                } catch (Exception e) {
                    Log.e(SENDING_ERROR, e.getMessage());
                }
            }
            return null;
        }
    }

    /**
     * Download Image Task
     */
    private class DownloadImageTask extends AsyncTask<UrlWithCallback, Void, Void> {

        protected Void doInBackground(UrlWithCallback... urls) {

            String urldisplay = urls[0].getUrl();
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(cloudUrl).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                urls[0].getDownloadPicCallback().downloadPicCallback(mIcon11);
            }catch(FileNotFoundException e) {
                InputStream in = null;
                String urldisplay2 = urls[0].getUrl();
                addFit();

                try {
                    in = new java.net.URL(cloudUrl).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                    urls[0].getDownloadPicCallback().downloadPicCallback(mIcon11);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
            catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }

    private void addFit() {
        int count = 0;
        for(int i = cloudUrl.length() - 1; i >= 0; --i) {
            if(cloudUrl.charAt(i) == '/') {
                count++;
            }
            if(count == 2) {
                cloudUrl = new StringBuilder(cloudUrl).insert(i, fit).toString();
                Log.e("Vamos ver", cloudUrl);
                return;
            }

        }
    }



}
