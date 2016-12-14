package br.lavid.pamin.com.pamin.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import br.lavid.pamin.com.pamin.utils.PaminAPI;

/**
 * Created by araujojordan on 03/07/15.
 */
public class User {

    private static final String PREF_USER = "USER_INFO";
    private static final String PREF_NAME = "user_name";
    private static final String PREF_PASS = "user_pass";
    private static final String PREF_EMAIL = "user_email";
    private static final String PREF_ID = "user_id";
    private static final String PREF_TOKEN = "user_token";
    private static final String PREF_BACKGROUND = "user_background";
    private static final String PREF_PICTURE = "user_picture";
    private static User instance;
    private Context ctx;
    private String name, email, password, token;
    private Location userLocation;
    private Bitmap background, userPicture;
    private int id;

    private User(Context ctx) {
        Log.v("User", "loading user");
        this.ctx = ctx;
        this.id = -1;
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREF_USER, Context.MODE_PRIVATE);
        if (sharedPref.getString(PREF_NAME, null) == null)
            return;

        name = sharedPref.getString(PREF_NAME, "username");
        password = sharedPref.getString(PREF_PASS, "pass");
        email = sharedPref.getString(PREF_EMAIL, "email@email.com");
        id = sharedPref.getInt(PREF_ID, -1);
        token = sharedPref.getString(PREF_TOKEN, "token");
        userPicture = getPictureFromString(sharedPref.getString(PREF_PICTURE, ""));
        background = getPictureFromString(sharedPref.getString(PREF_BACKGROUND, ""));

        Log.v("User", "user name: " + name);
        Log.v("User", "user email:" + email);
    }

    public static synchronized User getInstance(Context ctx) {
        if (instance == null)
            instance = new User(ctx);
        return instance;
    }

    private Bitmap getPictureFromString(String base64) {
        if (!base64.equalsIgnoreCase("")) {
            byte[] b = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return null;
    }

    public void setUserPicture(Bitmap userPicture, Context ctx) {
        this.userPicture = userPicture;

        SharedPreferences shre = ctx.getSharedPreferences(PREF_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = shre.edit();
        edit.putString(PREF_PICTURE, getStringFromBitmap(userPicture));
        edit.commit();
    }

    public void setBackground(Bitmap background, Context ctx) {
        this.background = background;

        SharedPreferences shre = ctx.getSharedPreferences(PREF_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = shre.edit();
        edit.putString(PREF_BACKGROUND, getStringFromBitmap(background));
        edit.commit();
    }

    private String getStringFromBitmap(Bitmap realImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        realImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);

    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public Bitmap getBackground() {
        return background;
    }

    public Bitmap getUserPicture() {
        return userPicture;
    }

    public boolean hasUser() {
        return id != -1;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public int getId() {
        return id;
    }

    /**
     * @param name     name of the user (at last 4 digits)
     * @param password password of the user (at last 8 digits)
     * @param email    email of the user (need to be a valid email)
     * @param callback will return true if the Pamin API accepted the register
     * @throws IllegalArgumentException if name/pass/email are not valid
     */
    public void registerUser(String name, String password, String email, final CompleteCallback callback) throws IllegalArgumentException {
        if (name == null || name.length() < 4)
            throw new IllegalArgumentException("Digite seu nome e sobrenome");

        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("Senha muito pequena!");

        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" //EMAIL CHECKER
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        if (!pattern.matcher(email).matches())
            throw new IllegalArgumentException("E-mail inválido!");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("email", email);
            jsonObject.accumulate("password", password);
            jsonObject.accumulate("name", name);
        } catch (JSONException error) {
            Log.e("JsonError:", error.getMessage());
        }

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.POST, PaminAPI.API_PAMIN + PaminAPI.API_SIGNUP, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        onPostSignup(jsonObject, callback);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Content-Type", "application/json");
                return header;
            }
        };

        RequestQueue mRequestQueue = Volley.newRequestQueue(ctx);
        mRequestQueue.add(jor);
    }

    public void loginUser(String email, String password, final CompleteCallback callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject userObject = new JSONObject();
            userObject.accumulate("email", email);
            userObject.accumulate("password", password);
            jsonObject.accumulate("api_user", userObject);

        } catch (JSONException error) {
            Log.e("JsonError:", error.getMessage());
        }

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.POST, PaminAPI.API_PAMIN + PaminAPI.API_SIGNIN, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        onPostSignin(jsonObject, callback);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Content-Type", "application/json");
                return header;
            }
        };

        RequestQueue mRequestQueue = Volley.newRequestQueue(ctx);
        mRequestQueue.add(jor);

//        new POSTAsyncTask(jsonObject, PaminAPI.API_PAMIN + PaminAPI.API_SIGNIN, callback).execute();
    }

    private void setUser(int id, String name, String email, String password, String token) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.token = token;

        saveOnDevice();
    }

    public void logoffUser() {
        this.id = -1;
        this.name = null;
        this.email = null;
        this.password = null;
        this.token = null;
        this.background = null;
        this.userPicture = null;

        SharedPreferences sharedPref = ctx.getSharedPreferences(PREF_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
    }

    private void saveOnDevice() {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREF_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(PREF_NAME, name);
        editor.putString(PREF_PASS, password);
        editor.putString(PREF_EMAIL, email);
        editor.putString(PREF_TOKEN, token);
        editor.putInt(PREF_ID, id);

        editor.apply();
    }

    private void onPostSignup(JSONObject jsonResult, CompleteCallback callback) { //REGISTRO
        int local_id = -1;
        String local_name = "", local_password = "", local_token = "", local_email = "";

        try {
            if (jsonResult.has("errors")) {
                Log.e("SignUp", "errors");
                JSONArray arrayErrors = jsonResult.getJSONArray("errors");
                for (int i = 0; i < arrayErrors.length(); i++) {
                    Toast.makeText(ctx,
                            (String) arrayErrors.get(i),
                            Toast.LENGTH_SHORT).show();
                }
                callback.completeCallback(false);
                return;
            }
            if (jsonResult.has("user")) {
                Log.v("SignUp", "New User");
                JSONObject newUser = jsonResult.getJSONObject("user");
                local_id = newUser.getInt("id");
                local_name = newUser.getString("name");
                local_email = newUser.getString("email");
//                    local_password = newUser.getString("password");
                Log.v("SignUp", "New User");
                setUser(local_id, local_name, local_email, local_password, "");
                Log.v("SignUp", "save complete!");
                callback.completeCallback(true);
            } else {
                name = null;
                password = null;
                email = null;
                id = -1;
                callback.completeCallback(false);
            }
        } catch (JSONException e) {
            Log.v("SignUp", "JSON EXCEPTION: " + e.getMessage());
            callback.completeCallback(false);
        }
    }

    private void onPostSignin(JSONObject jsonResult, CompleteCallback callback) { //LOGIN
        try {
            JSONObject jsonUser = (JSONObject) jsonResult.get("user");
            setUser(jsonUser.getInt("id"),
                    jsonUser.getString("name"),
                    jsonUser.getString("email"),
                    password,
                    jsonResult.getJSONObject("authentication_headers").getString("user_token"));
            callback.completeCallback(true);

        } catch (JSONException e) {
            Log.e("Login", e.getMessage());
            callback.completeCallback(false);
        }
    }

    public interface CompleteCallback {
        void completeCallback(boolean successful);
    }

}
