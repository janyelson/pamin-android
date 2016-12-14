package br.lavid.pamin.com.pamin.utils;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Created by araujojordan on 19/06/15.
 */
public class TabletFeatures {

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
//        return context.getResources().getBoolean(R.bool.isTablet);
    }
}
