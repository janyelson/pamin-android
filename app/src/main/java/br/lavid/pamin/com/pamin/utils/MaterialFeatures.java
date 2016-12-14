package br.lavid.pamin.com.pamin.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by araujojordan on 11/06/15.
 */
public class MaterialFeatures {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void actionBarColor(AppCompatActivity act, int color) {
        if (Build.VERSION.SDK_INT > 19) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
            if (act.getSupportActionBar() != null)
                act.getSupportActionBar().setElevation(0);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void navigationBarColor(AppCompatActivity act, int color) {
        if (Build.VERSION.SDK_INT > 19) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setNavigationBarColor(color);
        }
    }

    public static void hideKeyboard(Activity ctx) {
        // Check if no view has focus:
        View view = ctx.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


}
