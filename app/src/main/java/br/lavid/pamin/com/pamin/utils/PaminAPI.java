package br.lavid.pamin.com.pamin.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import br.lavid.pamin.com.pamin.models.CloudnaryPicture;
import br.lavid.pamin.com.pamin.models.CulturalRegister;
import br.lavid.pamin.com.pamin.models.User;

/**
 * Created by araujojordan on 13/06/15.
 * The functions to get json objects from the Pamin API
 * For now:
 * <p/>
 * getById()
 * getAll()
 * <p/>
 * Implement the callbacks to get the registers when the functions will over
 */
public class PaminAPI {

    public final static String API_PAMIN = "http://pamin.lavid.ufpb.br:3333/api";

    public final static String API_REGISTERS = "/registers";
    public final static String API_SIGNUP = "/users.json";
    public final static String API_SIGNIN = "/auth/sign_in";

    private RequestQueue mRequestQueue;

    /**
     * Convert Java Date to JSON format
     *
     * @param date the Java Date
     * @return if not null, return the Date in Json Format, ex: 2014-11-30T00:00:00.000Z
     */
    public static String convertDateToJsonDate(Date date) {
        try {
            String result = "";
            result += date.getYear() + "-";
            result += date.getMonth() + "-";
            result += date.getDay() + "T";
            result += date.getHours() + ":";
            result += date.getMinutes() + ":";
            result += date.getSeconds() + ".000Z";
            return result;
        } catch (Exception error) {
            return null;
        }
    }

    public static int getCategoryID(String category) {
        switch (category) {
            case "Pessoas":
                return 1;
            case "Lugares":
                return 2;
            case "Celebrações":
                return 3;
            case "Saberes":
                return 4;
            case "Formas de Expressão":
                return 5;
            case "Objetos":
                return 6;
            default:
                return 2;
        }
    }

    /**
     * Get all registers from registers web database
     *
     * @param jsonCallback
     */
    public void getAll(final GetAllCallback jsonCallback, Context ctx) {

        JsonArrayRequest jsObjRequest = new JsonArrayRequest(API_PAMIN + API_REGISTERS, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray jsonResponse) {

                LinkedList<CulturalRegister> culturalRegisters = new LinkedList<>();

                try {
                    for (int index = 0; index < jsonResponse.length(); index++) {
                        JSONObject jsonObject = jsonResponse.getJSONObject(index);

                        long idCulturalRegister;
                        String title, description, promotor, promotor_contact, category, where;
                        double latitude, longitude, price;
                        Date startDate, endDate;

                        idCulturalRegister = jsonObject.getLong("id");
                        title = jsonObject.getString("what");
                        description = jsonObject.getString("description");
                        promotor = jsonObject.getString("promotor");
                        promotor_contact = jsonObject.getString("promotor_contact");
                        latitude = jsonObject.getDouble("latitude");
                        longitude = jsonObject.getDouble("longitude");
                        category = jsonObject.getJSONObject("category").getString("name");
                        where = jsonObject.getString("where");
                        if (!jsonObject.isNull("price"))
                            price = jsonObject.getDouble("price");
                        else
                            price = -1.0d;

                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

                        if (!jsonObject.getString("start_date").equals("null"))
                            startDate = df.parse(jsonObject.getString("start_date"));
                        else
                            startDate = null;
                        if (!jsonObject.getString("end_date").equals("null"))
                            endDate = df.parse(jsonObject.getString("end_date"));
                        else
                            endDate = null;

                        CulturalRegister culturalRegister = new CulturalRegister(idCulturalRegister,
                                promotor, promotor_contact, title, description,
                                null, startDate, endDate, price, latitude, longitude, category, where);

//                    for (int i = 0; i < jsonObject.getJSONArray("pictures_videos").length(); i++) {
//                        if (jsonObject.getJSONArray("pictures_videos").get(i).toString().endsWith(".jpg") ||
//                                jsonObject.getJSONArray("pictures_videos").get(i).toString().endsWith(".jpeg") ||
//                                jsonObject.getJSONArray("pictures_videos").get(i).toString().endsWith(".png") ||
//                                jsonObject.getJSONArray("pictures_videos").get(i).toString().endsWith(".webm"))
//                            culturalRegister.addPicture(new CloudnaryPicture(jsonObject.getJSONArray("pictures_videos").get(i).toString()));
//                    }

                        culturalRegisters.add(culturalRegister);
                    }
                    jsonCallback.JsonReturn(culturalRegisters);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        });

        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(ctx);
        mRequestQueue.add(jsObjRequest);
    }

    /**
     * Send a culturalRegister in the json and exptect an response from callback
     *
     * @param json         json with the culturalregister
     * @param sendCallback The callback that will call when the server response
     */
    public void sendNewCultRegister(final User user, JSONObject json, Context ctx, final SendRegisterCallback sendCallback) {

        Log.v("PaminAPI", json.toString());

        try {
            JsonObjectRequest jor = new JsonObjectRequest(Request.Method.POST, API_PAMIN + API_REGISTERS, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    Log.v("PaminAPI", jsonObject.toString());
                    sendResponse(jsonObject, sendCallback);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("PaminAPI", "ERROR: " + volleyError.getMessage());
                    sendResponse(null, sendCallback);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<String, String>();
                    header.put("Content-Type", "application/json");
                    header.put("X-User-Email", user.getEmail());
                    header.put("X-User-Token", user.getToken());
                    return header;
                }
            };
            if (mRequestQueue == null)
                mRequestQueue = Volley.newRequestQueue(ctx);

            mRequestQueue.add(jor);

        } catch (Exception error) {
            Log.e("PAMINAPI", error.getMessage());
        }


    }

    private void sendResponse(JSONObject response, SendRegisterCallback callback) {
        if (response == null) {
            Log.e("Error", "Doesn't have the expect answer in result");
            callback.registerCallback(false, null);
            return;
        }

        Log.v("Result", response.toString());
        CulturalRegister culturalRegister;

        try {
            JSONObject jsonResponse = response;

            long idCulturalRegister;
            String title, description, promotor, promotor_contact, category, where;
            double latitude, longitude, price;
            Date startDate, endDate;

            idCulturalRegister = jsonResponse.getLong("id");
            title = jsonResponse.getString("what");
            description = jsonResponse.getString("description");
            promotor = jsonResponse.getString("promotor");
            promotor_contact = jsonResponse.getString("promotor_contact");
            latitude = jsonResponse.getDouble("latitude");
            longitude = jsonResponse.getDouble("longitude");
            category = jsonResponse.getJSONObject("category").getString("name");
            where = jsonResponse.getString("where");
            if (!jsonResponse.isNull("price"))
                price = jsonResponse.getDouble("price");
            else
                price = -1.0d;

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

            if (!jsonResponse.getString("start_date").equals("null"))
                startDate = df.parse(jsonResponse.getString("start_date"));
            else
                startDate = null;
            if (!jsonResponse.getString("end_date").equals("null"))
                endDate = df.parse(jsonResponse.getString("end_date"));
            else
                endDate = null;

            culturalRegister = new CulturalRegister(idCulturalRegister,
                    promotor, promotor_contact, title, description,
                    null, startDate, endDate, price, latitude, longitude, category, where);

            for (int i = 0; i < jsonResponse.getJSONArray("pictures_videos").length(); i++) {
                if (jsonResponse.getJSONArray("pictures_videos").get(i).toString().endsWith(".jpg") ||
                        jsonResponse.getJSONArray("pictures_videos").get(i).toString().endsWith(".jpeg") ||
                        jsonResponse.getJSONArray("pictures_videos").get(i).toString().endsWith(".png") ||
                        jsonResponse.getJSONArray("pictures_videos").get(i).toString().endsWith(".webm"))
                    culturalRegister.addPicture(new CloudnaryPicture(jsonResponse.getJSONArray("pictures_videos").get(i).toString()));
            }

            callback.registerCallback(true, culturalRegister);

        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            callback.registerCallback(false, null);
        }
    }

    /**
     * Update some CulturalRegister
     *
     * @param user         the user that is updating the register
     * @param cultReg      the cultural register to be updating
     * @param sendCallback the callback to be implemented in the call
     */
    public void updateRegister(User user, final CulturalRegister cultReg, final SendRegisterCallback sendCallback) {
        JSONObject json = new JSONObject();
        try {
            json.put("id", cultReg.getIdCulturalRegister());
            json.put("what", cultReg.getTitle());
            json.put("where", cultReg.getWhere());
            json.put("start_date", convertDateToJsonDate(cultReg.getStartDate()));
            json.put("end_date", convertDateToJsonDate(cultReg.getEndDate()));
            json.put("price", cultReg.getPrice());
            json.put("promotor", cultReg.getPromoter());
            json.put("promotor_contact", cultReg.getPromoterContact());
            json.put("latitude", cultReg.getLatitude());
            json.put("longitude", cultReg.getLongitude());
            json.put("what", getCategoryID(cultReg.getCategory()));
            json.put("description", cultReg.getDescription());

            JSONArray picsVidsJson = new JSONArray();
            for (CloudnaryPicture cloudPic : cultReg.getPictures())
                picsVidsJson.put(cloudPic.getCompleteUrl());
            json.put("pictures_videos", picsVidsJson);

            new JsonObjectRequest(Request.Method.PUT, API_PAMIN + API_REGISTERS, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    Log.v("PaminAPI", "Sending Complete");
                    sendCallback.registerCallback(true, cultReg);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("PaminAPI", "ERROR: " + volleyError.getMessage());
                    sendCallback.registerCallback(false, cultReg);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    /**
     * Callback with the list recovered
     */
    public interface GetAllCallback {
        void JsonReturn(LinkedList<CulturalRegister> culturalRegister);
    }

    /**
     * The interface with the callback of the culturalRegister
     */
    public interface GetByIdCallback {
        void JsonReturn(CulturalRegister culturalRegister);
    }

    /**
     * The interface with the callback of the POST a culturalRegister
     * This culturalRegister will have the id of the database
     */
    public interface SendRegisterCallback {
        void registerCallback(boolean successful, CulturalRegister culturalRegister);
    }

//    /**
//     * Async load all registers from pamin web api
//     */
//    private class LoadAllAsync extends AsyncTask<GetAllCallback, String, String> {
//
//        String result = "";
//        LinkedList<CulturalRegister> culturalRegisters = new LinkedList<>();
//
//        @Override
//        protected String doInBackground(GetAllCallback... params) {
//
//
//
//            return null;
//        }
//
//
//    }
}
