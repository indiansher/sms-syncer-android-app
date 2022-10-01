package com.thelioncompany.smssyncer.util;

import android.util.Base64;
import android.util.Log;

public class Base64Util {

    private static final String TAG = "Base64Util";

    public static boolean isInputBase64(String input) {
        try {
            Base64.decode(input, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    public static String decodeBase64String(String encodedString) {
        String decodedString = "";
        try {
            byte[] decodedStringBytes = Base64.decode(encodedString, Base64.DEFAULT);
            decodedString = new String(decodedStringBytes);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.toString());
        }
        return decodedString;
    }

    public static String encodeStringToBase64(String str) {
        if (str == null) {
            return "";
        }
        return Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
    }
}
