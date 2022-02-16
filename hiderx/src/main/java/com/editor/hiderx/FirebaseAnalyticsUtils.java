package com.editor.hiderx;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseAnalyticsUtils {

    public static void sendScreen(Activity activity , String screenName){
        try {
            if (activity!=null) {
                FirebaseAnalytics mFirebaseAnalytics;
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, activity.getClass().getSimpleName());
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            }
        }catch (Exception e){
        }
    }


    public static void sendEvent(Context context, String click_event, String event){
        if (context!=null) {
            FirebaseAnalytics mFirebaseAnalytics;
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.ITEM_NAME, click_event);
            mFirebaseAnalytics.logEvent(event, params);
        }
    }
}
