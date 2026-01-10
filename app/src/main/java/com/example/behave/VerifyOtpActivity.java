package com.example.behave;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class VerifyOtpActivity extends AppCompatActivity {

    private EditText etOtp;
    private Button btnVerifyOtp;
    private String verificationId;
    private FirebaseAuth mAuth;
    private static final String TAG = "VerifyOtpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        mAuth = FirebaseAuth.getInstance();
        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        verificationId = getIntent().getStringExtra("verificationId");

        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
    }

    private void verifyOtp() {
        String otp = etOtp.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        startActivity(new Intent(VerifyOtpActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(VerifyOtpActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
