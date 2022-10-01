package com.thelioncompany.smssyncer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.thelioncompany.smssyncer.data.SharedPrefRepository;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "main activity";
    private static final int REQUEST_CODE = 1122;

    private LinearLayout introLayout, sender_title_layout, receiver_title_layout;
    private ConstraintLayout sender_config_layout, receiver_config_layout;
    private TextView myFcmTokenText;
    private EditText targetFcmTokenText, serviceAccountJsonText;
    private Button submitBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.bindComponents();
        this.bindListeners();
        this.loadIntroScreen();
    }

    private void bindComponents() {
        // Layouts
        introLayout = (LinearLayout) findViewById(R.id.intro_layout);
        sender_title_layout = (LinearLayout) findViewById(R.id.title_sender_layout);
        receiver_title_layout = (LinearLayout) findViewById(R.id.title_receiver_layout);
        sender_config_layout = (ConstraintLayout) findViewById(R.id.sender_config_layout);
        receiver_config_layout = (ConstraintLayout) findViewById(R.id.receiver_config_layout);

        // Text Views
        myFcmTokenText = (TextView) findViewById(R.id.my_fcm_token);

        // Edit Texts
        targetFcmTokenText = (EditText) findViewById(R.id.target_fcm_token);
        serviceAccountJsonText = (EditText) findViewById(R.id.service_account_json);

        // Button
        submitBtn = (Button) findViewById(R.id.submit_btn);
    }

    private void bindListeners() {
        sender_title_layout.setOnClickListener(view -> loadSenderConfigScreen());
        receiver_title_layout.setOnClickListener(view -> loadReceiverConfigScreen());
        submitBtn.setOnClickListener(view -> saveSenderConfigData());
    }


    private void loadIntroScreen() {
        introLayout.setVisibility(View.VISIBLE);
        sender_config_layout.setVisibility(View.GONE);
        receiver_config_layout.setVisibility(View.GONE);
    }

    private void loadSenderConfigScreen() {
        introLayout.setVisibility(View.GONE);
        sender_config_layout.setVisibility(View.VISIBLE);
        receiver_config_layout.setVisibility(View.GONE);

        checkForSenderPermissions();

        targetFcmTokenText.setText(SharedPrefRepository.getTargetFCMToken(this.getApplicationContext()));
        serviceAccountJsonText.setText(SharedPrefRepository.getServiceAccountJson(this.getApplicationContext()));
    }

    private void loadReceiverConfigScreen() {
        introLayout.setVisibility(View.GONE);
        sender_config_layout.setVisibility(View.GONE);
        receiver_config_layout.setVisibility(View.VISIBLE);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();
                    if (token == null) {
                        token = "";
                    }
                    myFcmTokenText.setText(token);
                });
    }

    private void saveSenderConfigData() {
        SharedPrefRepository.saveServiceAccountJson(getApplicationContext(), serviceAccountJsonText.getText().toString());
        SharedPrefRepository.saveTargetFcmToken(getApplicationContext(), targetFcmTokenText.getText().toString());
        Toast.makeText(getApplicationContext(), R.string.input_done_msg, Toast.LENGTH_SHORT).show();
    }

    void checkForSenderPermissions() {
        // Request permissions from user if App doesn't have permissions for receiving SMS or INTERNET
        if (
                (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) ||
                        (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.INTERNET}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            boolean gotAllPermissions = grantResults.length > 0;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission granted: " + permissions[i]);
                } else {
                    gotAllPermissions = false;
                    Log.d(TAG, "Permission denied: " + permissions[i]);
                }
            }
            if (!gotAllPermissions) {
                finish();
            }
        }
    }
}