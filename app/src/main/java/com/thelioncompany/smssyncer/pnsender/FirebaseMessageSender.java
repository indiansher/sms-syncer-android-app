package com.thelioncompany.smssyncer.pnsender;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.util.Strings;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.thelioncompany.smssyncer.data.SharedPrefRepository;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class FirebaseMessageSender {

    private static final String TAG = "FirebaseMessageSender";
    private static final String PROJECT_ID = "sms-syncer";
    private static final String BASE_URL = "https://fcm.googleapis.com";
    private static final String FCM_SEND_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/messages:send";
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = {MESSAGING_SCOPE};

    private String accessToken;
    private String targetFcmToken;

    public FirebaseMessageSender(Context context) {
        this.targetFcmToken = SharedPrefRepository.getTargetFCMToken(context);
        this.accessToken = generateAccessToken(context);
    }

    private static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }

    private static String generateAccessToken(Context context) {

        String savedAccessToken = SharedPrefRepository.getAccessToken(context);
        if (!Strings.isEmptyOrWhitespace(savedAccessToken)) {
            return savedAccessToken;
        }

        try {
            String serviceAccountJson = SharedPrefRepository.getServiceAccountJson(context);
            InputStream stream = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(stream)
                    .createScoped(Arrays.asList(SCOPES));
            AccessToken accessToken = googleCredentials.refreshAccessToken();
            String token = accessToken.getTokenValue();
            Long expirationMillis = accessToken.getExpirationTime().getTime();
            SharedPrefRepository.saveAccessToken(context, token, expirationMillis);
            return token;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return "";
    }

    public void pushNoti(Notification noti, Context context) {

        JSONObject payload = new JSONObject();
        JSONObject message = new JSONObject();
        JSONObject notification = new JSONObject();

        try {

            // notification
            notification.put("title", noti.getTitle());
            notification.put("body", noti.getBody());

            // message
            message.put("token", this.targetFcmToken);
            message.put("notification", notification);

            //payload
            payload.put("message", message);

            // Send
            URL url = new URL(BASE_URL + FCM_SEND_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json; UTF-8");
            conn.setDoOutput(true);

            // Send FCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(payload.toString().getBytes());
            outputStream.close();

            // Read FCM response status
            int status = conn.getResponseCode();
            Log.d(TAG, "response status: " + status);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
