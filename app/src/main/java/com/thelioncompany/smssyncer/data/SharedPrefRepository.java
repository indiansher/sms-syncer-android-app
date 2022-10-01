package com.thelioncompany.smssyncer.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefRepository {

    private static final String TAG = "SharedPrefRepository";

    private static final String SHARED_PREFERENCES_FILE_NAME = "com.thelioncompany.smssyncer.data.shared_pref";

    private static final Long TEN_SECONDS_IN_MILLIS = 10000L;
    private static final String SHARED_PREF_SERVICE_ACCESS_TOKEN_EXPIRY = "ate";
    private static final String SHARED_PREF_SERVICE_ACCESS_TOKEN = "at";
    private static final String SHARED_PREF_SERVICE_ACCOUNT_JSON = "sad";
    private static final String SHARED_PREF_TARGET_FCM_TOKEN = "tft";

    private static SharedPreferences getPref(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }

    public static void saveAccessToken(Context context, String accessToken, Long expirationMillis) {
        SharedPreferences.Editor prefEditor = getPref(context).edit();
        prefEditor.putString(SHARED_PREF_SERVICE_ACCESS_TOKEN, accessToken);
        prefEditor.putLong(SHARED_PREF_SERVICE_ACCESS_TOKEN_EXPIRY, expirationMillis);
        prefEditor.apply();
    }

    public static void saveTargetFcmToken(Context context, String targetFCMToken) {
        SharedPreferences.Editor prefEditor = getPref(context).edit();
        prefEditor.putString(SHARED_PREF_TARGET_FCM_TOKEN, targetFCMToken);
        prefEditor.apply();
    }

    public static void saveServiceAccountJson(Context context, String serviceAccountJson) {
        SharedPreferences.Editor prefEditor = getPref(context).edit();
        prefEditor.putString(SHARED_PREF_SERVICE_ACCOUNT_JSON, serviceAccountJson);
        prefEditor.apply();
        saveAccessToken(context, "", 0L);
    }

    public static String getAccessToken(Context context) {
        String accessToken = getPref(context).getString(SHARED_PREF_SERVICE_ACCESS_TOKEN, "");
        Long expirationMillis = getPref(context).getLong(SHARED_PREF_SERVICE_ACCESS_TOKEN_EXPIRY, 0L);
        if (System.currentTimeMillis() >= (expirationMillis - TEN_SECONDS_IN_MILLIS)) { // assuming some buffer
            accessToken = "";
        }
        return accessToken;
    }

    public static String getTargetFCMToken(Context context) {
        return getPref(context).getString(SHARED_PREF_TARGET_FCM_TOKEN, "");
    }

    public static String getServiceAccountJson(Context context) {
        return getPref(context).getString(SHARED_PREF_SERVICE_ACCOUNT_JSON, "{}");
    }
}
