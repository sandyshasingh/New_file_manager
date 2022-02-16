package com.editor.hiderx

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.text.TextUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemotConfigUtils {

    const val APP_RATEUS_MSG = "rateus_msg"
    const val disable_back_onrateus = "disable_back_on_rateus"
    const val APP_RATEUS_POSITIVE_BUTTON = "rateus_button"


    fun setFirebaseRemoteConfig(context: Activity?) {
        if (context == null) {
            return
        }
        FirebaseApp.initializeApp(context)
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(R.xml.remote_config_default)
        Handler().postDelayed({ fetchFirebaseRemoteConfig(context) }, 1000L)
    }

    fun fetchFirebaseRemoteConfig(context: Activity?) {
        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(context!!) { task ->
                    if (task.isSuccessful) {
                        val updated = task.result
                    }
                }
    }

    fun getRateUsMessage(activity: Activity): String? {
        try {
            FirebaseApp.initializeApp(activity.applicationContext)
            val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            var rateUsmessage: String = mFirebaseRemoteConfig.getString(APP_RATEUS_MSG)
            if (TextUtils.isEmpty(rateUsmessage)) {
                rateUsmessage = activity.resources.getString(R.string.rate_us_title2)
            }
            return rateUsmessage
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log(e.toString())
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        return activity.resources.getString(R.string.rate_us_title2)
    }

    fun getDisableBackOnRateUsFlag(appCompatActivity: Context): Boolean {
        if (BuildConfig.DEBUG) {
            return false
        }
        try {
            FirebaseApp.initializeApp(appCompatActivity.applicationContext)
            val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val enableValue: String? =
                mFirebaseRemoteConfig.getString(disable_back_onrateus)
            return !(enableValue != null && enableValue.equals("false", ignoreCase = true))
        } catch (e: java.lang.Exception) {
            FirebaseCrashlytics.getInstance().log(e.toString())
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        return true
    }

    fun getRateUsPositiveButtonMessage(activity: Activity): String {
        try {
            FirebaseApp.initializeApp(activity.applicationContext)
            val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            var rateUsmessage =
                mFirebaseRemoteConfig.getString(APP_RATEUS_POSITIVE_BUTTON)
            if (TextUtils.isEmpty(rateUsmessage)) {
                rateUsmessage = "LIKE"
            }
            return rateUsmessage
        } catch (e: java.lang.Exception) {
            FirebaseCrashlytics.getInstance().log(e.toString())
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        return "LIKE"
    }
}