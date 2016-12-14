package br.lavid.pamin.com.pamin.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.lavid.pamin.com.pamin.models.CulturalRegister;

/**
 * Created by Jordan on 28/08/2015.
 */
public class GoogleAPI {

    public final static String GOOGLE_MAPS_API = "https://maps.googleapis.com/maps/api/";
    public final static String GOOGLE_IMAGE_API = "http://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=5&q=";
    public final static String GOOGLE_GEOCODING = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";

    public final static String PICTURE_SIZE = "400";
    public final static String SPACE_CODE = "%20";

    public final static String STREETVIEW = "streetview?size=" + PICTURE_SIZE + "x" + PICTURE_SIZE + "&";
    public final static String STREETVIEW_END = "&fov=90&heading=235&pitch=10&sensor=false";

    public final static String PLACES = "place/search/json?";
    public final static String PLACES_END = "&radius=300&sensor=false&key=AIzaSyA68OlLSdY0nr8L77MgBCUpEu4b3tGSebA";

    public final static String GEO_END = "&location_type=APPROXIMATE&&result_type=locality&sensor=true&key=AIzaSyA68OlLSdY0nr8L77MgBCUpEu4b3tGSebA";

    private BitmapFactory.Options options;
    private RequestQueue queue;

    private Context ctx;

    public GoogleAPI(Context ctx) {
        queue = Volley.newRequestQueue(ctx);
        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        this.ctx = ctx;
    }

    public void getCity(final CulturalRegister cultReg, final SearchCityCallback update) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v("GetCity", cultReg.getTitle());
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                        GOOGLE_GEOCODING + cultReg.getLatitude() + "," + cultReg.getLongitude() + GEO_END, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String city = response.getJSONArray("results").getJSONObject(0)
                                            .getJSONArray("address_components").getJSONObject(0)
                                            .getString("short_name");
                                    update.cityComplete(cultReg, city);

                                } catch (JSONException e) {
                                    Log.e("GetCity", "City not found, " + e.getMessage());
                                    update.cityComplete(cultReg, null);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("GAPI", "getCity " + error.getMessage());
                                update.cityComplete(cultReg, null);
                            }
                        });
                queue.add(request);
            }
        }).run();
    }

    public void searchForImages(final String city, final CulturalRegister cultReg, final GoogleImageCallback update) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                Log.v("searchForImages", cultReg.getTitle());
                String place = cultReg.getTitle();
                if (city != null)
                    place += city;
                String url = (GOOGLE_IMAGE_API + place).replace(" ", SPACE_CODE);
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                String[] urls;
                                try {
                                    JSONArray array = response.getJSONObject("responseData").getJSONArray("results");
                                    urls = new String[array.length()];
                                    for (int i = 0; i < array.length(); i++)
                                        urls[i] = array.getJSONObject(i).getString("url");
                                    update.searchComplete(cultReg, urls);
                                } catch (JSONException e) {
                                    Log.e("GetCity", "search error, " + e.getMessage());
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("GAPI", "searchForImages " + error.getMessage());
                            }
                        });
                queue.add(request);
            }
        }).start();
    }

    public void downloadImage(final CulturalRegister cultReg, final DownloadCallback update, final String url, final String[] urls, final int number) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmapPlacesPhoto) {
                                    bitmapPlacesPhoto = PictureTransformation.getCroppedBitmap(bitmapPlacesPhoto);
                                    int size[] = calculateBitmapSize(bitmapPlacesPhoto);
                                    bitmapPlacesPhoto = Bitmap.createScaledBitmap(bitmapPlacesPhoto, size[0], size[1], false);

                                    Log.v("DImgTsk", "Picture for " + number + " found, saving " + url);
                                    cultReg.setDevicePictures(bitmapPlacesPhoto, number, ctx);
                                    update.pictureComplete(cultReg, bitmapPlacesPhoto, number);
                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                    try {
                                        Log.e("DImgTsk", "Error 404 to found picture for " + number + "" +
                                                "\n URL: " + url +
                                                "\nNEXT TRY: " + urls[checkPosition(urls, url) + 1]);
                                        downloadImage(cultReg, update, urls[checkPosition(urls, url) + 1], urls, number);
                                    } catch (ArrayIndexOutOfBoundsException out) {
                                    }
                                }
                            });
                    queue.add(request);

                } catch (Exception e) {
                    Log.e("DImgTsk", "Error to download " + url);
                }
            }
        }).run();
    }

    private int checkPosition(String[] urls, String url) {
        for (int position = 0; position < urls.length; position++)
            if (urls[position].equals(url))
                return position;

        return urls.length;
    }

    private int[] calculateBitmapSize(Bitmap bitmap) {
        final int WANTED_SIZE = 400;

        int size[] = new int[2];

        if (bitmap.getWidth() > WANTED_SIZE) {
            size[0] = bitmap.getWidth() / (bitmap.getWidth() / WANTED_SIZE);
        } else {
            size[0] = bitmap.getWidth();
        }

        if (bitmap.getHeight() > WANTED_SIZE) {
            size[1] = bitmap.getHeight() / (bitmap.getHeight() / WANTED_SIZE);
        } else
            size[1] = bitmap.getHeight();

        return size;
    }

    //INTERFACE
    public interface SearchCityCallback {
        void cityComplete(CulturalRegister culturalRegister, String city);
    }

    public interface GoogleImageCallback {
        void searchComplete(CulturalRegister culturalRegister, String[] urls);
    }

    public interface DownloadCallback {
        void pictureComplete(CulturalRegister culturalRegister, Bitmap bitmap, int number);
    }


}
