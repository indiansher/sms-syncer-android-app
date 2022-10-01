package com.thelioncompany.smssyncer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.thelioncompany.smssyncer.data.SharedPrefRepository;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "main activity";
    private static final int REQUEST_CODE = 1122;

    private TextView myFcmTokenText;
    private EditText targetFcmTokenText;
    private EditText serviceAccountJsonText;
    private Button submitBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForSmsReceivePermissions();

        myFcmTokenText = (TextView) findViewById(R.id.fcm_token);
        setPrintFCMTokenHook();

        targetFcmTokenText = (EditText) findViewById(R.id.target_fcm_token);
        targetFcmTokenText.setText(SharedPrefRepository.getTargetFCMToken(this.getApplicationContext()));

        serviceAccountJsonText = (EditText) findViewById(R.id.service_account_json);
        serviceAccountJsonText.setText(SharedPrefRepository.getServiceAccountJson(this.getApplicationContext()));

        submitBtn = (Button) findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPrefRepository.saveServiceAccountJson(getApplicationContext(), serviceAccountJsonText.getText().toString());
                SharedPrefRepository.saveTargetFcmToken(getApplicationContext(), targetFcmTokenText.getText().toString());
                Toast.makeText(getApplicationContext(), R.string.input_done_msg, Toast.LENGTH_SHORT).show();
            }
        });
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

    void checkForSmsReceivePermissions() {
        // Request permissions from user if App doesn't have permissions for receiving SMS or INTERNET
        if (
                (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) ||
                        (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.INTERNET}, REQUEST_CODE);
        }
    }

    void setPrintFCMTokenHook() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        myFcmTokenText.setText(token);
                    }
                });
    }
}