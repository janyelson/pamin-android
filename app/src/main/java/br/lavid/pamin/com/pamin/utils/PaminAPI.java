package br.lavid.pamin.com.pamin.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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
    //"http://pamin.lavid.ufpb.br:3333/api"
    public final static String API_PAMIN = "http://pamin.lavid.ufpb.br:3333/api";

    public final static String API_REGISTERS = "/registers";
    public final static String API_SIGNUP = "/users";
    public final static String API_SIGNIN = "/auth/sign_in";

    //private long final_id = 0;
    private static long aux1 = 0;
    //private static long aux2 = 0;

    private static ReentrantLock lock = new ReentrantLock(true);
    private static Condition WR = lock.newCondition();

    private RequestQueue mRequestQueue;

    /**
     * Convert Java Date to JSON format
     *
     * @param calendar the Java Date
     * @return if not null, return the Date in Json Format, ex: 2014-11-30T00:00:00.000Z
     */
    public static String convertDateToJsonDate(Calendar calendar) {
        try {
            String result = "";
            int month = calendar.get(Calendar.MONTH) + 1;
            result += calendar.get(Calendar.YEAR) + "-";
            result += month + "-";
            result += calendar.get(Calendar.DAY_OF_MONTH) + "T";
            result += calendar.get(Calendar.HOUR) + ":";
            result += calendar.get(Calendar.MINUTE) + ":";
            result += calendar.get(Calendar.SECOND) + ".000Z";

            //Log.e("Pamin Data", result);
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
                return 3;
        }
    }

    public static String getCategoryName(int ID) {
        switch (ID) {
            case 1:
                return "Pessoas";
            case 2:
                return "Lugares";
            case 3:
                return "Celebrações";
            case 4:
                return "Saberes";
            case 5:
                return "Formas de Expressão";
            case 6:
                return "Objetos";
            default:
                return "Celebrações";
        }
    }

    /**
     * Get all registers from registers web database
     *
     * @param jsonCallback
     */
    public void getAll(final GetAllCallback jsonCallback, Context ctx) throws InterruptedException {
        //lock.lock();
        //if(aux1 == 1) {WR.await();}
        //aux1 = 1;
            Log.e("getAll pegando tudo ", "Vamos ver 222");
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
                            Calendar startDate = Calendar.getInstance();
                            Calendar endDate = Calendar.getInstance();

                            idCulturalRegister = jsonObject.getLong("id");
                            title = jsonObject.getString("what");
                            description = jsonObject.getString("description");
                            promotor = jsonObject.getString("promotor");
                            promotor_contact = jsonObject.getString("promotor_contact");
                            latitude = jsonObject.getDouble("latitude");
                            longitude = jsonObject.getDouble("longitude");
                            //category = jsonObject.getJSONObject("category").getString("name");
                            category = getCategoryName(jsonObject.getJSONObject("category").getInt("id"));
                            where = jsonObject.getString("where");
                            if (!jsonObject.isNull("price"))
                                price = jsonObject.getDouble("price");
                            else
                                price = -1.0d;

                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

                            if (!jsonObject.getString("start_date").equals("null"))
                                startDate.setTime(df.parse(jsonObject.getString("start_date")));
                            else
                                startDate = null;
                            if (!jsonObject.getString("end_date").equals("null"))
                                endDate.setTime(df.parse(jsonObject.getString("end_date")));
                            else
                                endDate = null;

                            CulturalRegister culturalRegister = new CulturalRegister(idCulturalRegister,
                                    promotor, promotor_contact, title, description,
                                    null, startDate, endDate, price, latitude, longitude, category, where);

                            //for (int i = 0; i < jsonObject.getJSONArray("pictures").toString().length(); i++) {
                            //    if (jsonObject.getJSONArray("pictures").get(i).toString().endsWith(".jpg") ||
                            //            jsonObject.getJSONArray("pictures").get(i).toString().endsWith(".jpeg") ||
                            //            jsonObject.getJSONArray("pictures").get(i).toString().endsWith(".png") ||
                            //            jsonObject.getJSONArray("pictures").get(i).toString().endsWith(".webm"))
                            //          culturalRegister.addPicture(new CloudnaryPicture(jsonObject.getJSONArray("pictures").get(i).toString()));
                            //}
                            if (jsonObject.getString("pictures").endsWith(".jpg") || jsonObject.getString("pictures").endsWith(".jpeg") || jsonObject.getString("pictures").endsWith(".png") || jsonObject.getString("pictures").endsWith(".webp")) {
                                culturalRegister.addPicture(new CloudnaryPicture(jsonObject.getString("pictures")));
                            }

                            //if(final_id < idCulturalRegister) {
                            culturalRegisters.add(culturalRegister);
                            //    final_id = idCulturalRegister;
                            //}
                        }
                        jsonCallback.JsonReturn(culturalRegisters);
                        //aux1 = 0;
                        //if(lock.hasWaiters(WR)) {
                        //    WR.signal();
                        //}
                        //WR.signal();
                        //lock.unlock();

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
            //jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(50000000, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(jsObjRequest);

    }

    /**
     * Send a culturalRegister in the json and exptect an response from callback
     *
     * @param json         json with the culturalregister
     * @param sendCallback The callback that will call when the server response
     */
    public void sendNewCultRegister(final User user,final JSONObject json, Context ctx, final SendRegisterCallback sendCallback) {

        Log.v("PaminAPI", json.toString());

        try {
            JsonObjectRequest jor = new JsonObjectRequest(Request.Method.POST, API_PAMIN + API_REGISTERS, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    Log.v("PaminAPIonResponse", jsonObject.toString());
                    sendResponse(jsonObject, sendCallback);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("PaminAPI", "ERROR: " + volleyError.getCause());
                    Log.e("PaminAPI", "TimeoutError: " + (volleyError instanceof TimeoutError));
                    Log.e("PaminAPI", "NoConnectionError: " + (volleyError instanceof NoConnectionError));
                    Log.e("PaminAPI", "NetWorkError: " + (volleyError instanceof NetworkError));
                    Log.e("PaminAPI", "AuthFailureError: " + (volleyError instanceof AuthFailureError));
                    Log.e("PaminAPI", "ServerError: " + (volleyError instanceof ServerError));
                    Log.e("PaminAPI", "ParseError: " + (volleyError instanceof ParseError));
                    Log.e("PaminAPI", "NullPointerException: " + (volleyError.getCause() instanceof NullPointerException));
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
            //Date startDate, endDate;
            Calendar startDate = Calendar.getInstance();
            Calendar endDate = Calendar.getInstance();

            idCulturalRegister = jsonResponse.getLong("id");
            title = jsonResponse.getString("what");
            description = jsonResponse.getString("description");
            promotor = jsonResponse.getString("promotor");
            promotor_contact = jsonResponse.getString("promotor_contact");
            latitude = jsonResponse.getDouble("latitude");
            longitude = jsonResponse.getDouble("longitude");
            //category = jsonResponse.getJSONObject("category").getString("name");
            category = getCategoryName(jsonResponse.getJSONObject("category").getInt("id"));
            where = jsonResponse.getString("where");
            if (!jsonResponse.isNull("price"))
                price = jsonResponse.getDouble("price");
            else
                price = -1.0d;

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

            if (!jsonResponse.getString("start_date").equals("null"))
                startDate.setTime(df.parse(jsonResponse.getString("start_date")));
            else
                startDate = null;
            if (!jsonResponse.getString("end_date").equals("null"))
                endDate.setTime(df.parse(jsonResponse.getString("end_date")));
            else
                endDate = null;

            culturalRegister = new CulturalRegister(idCulturalRegister,
                    promotor, promotor_contact, title, description,
                    null, startDate, endDate, price, latitude, longitude, category, where);

            //for (int i = 0; i < jsonResponse.getJSONArray("pictures").length(); i++) {
            //    if (jsonResponse.getJSONArray("pictures").get(i).toString().endsWith(".jpg") ||
            //            jsonResponse.getJSONArray("pictures").get(i).toString().endsWith(".jpeg") ||
            //            jsonResponse.getJSONArray("pictures").get(i).toString().endsWith(".png") ||
            //            jsonResponse.getJSONArray("pictures").get(i).toString().endsWith(".webm"))
            //        culturalRegister.addPicture(new CloudnaryPicture(jsonResponse.getJSONArray("pictures").get(i).toString()));
            //}

            //Passando de Array para string - Apenas uma imagem
            if (jsonResponse.getString("pictures").endsWith(".jpg") ||
                    jsonResponse.getString("pictures").endsWith(".jpeg") ||
                    jsonResponse.getString("pictures").endsWith(".png") ||
                    jsonResponse.getString("pictures").endsWith(".webp"))
                culturalRegister.addPicture(new CloudnaryPicture(jsonResponse.getString("pictures")));

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
            //String picsVidsJson = null;
            JSONArray picsVidsJson = new JSONArray();
            for (CloudnaryPicture cloudPic : cultReg.getPictures()) {
                picsVidsJson.put(cloudPic.getCompleteUrl());
                //picsVidsJson = cloudPic.getCompleteUrl();
            }

            json.put("pictures", picsVidsJson);

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
