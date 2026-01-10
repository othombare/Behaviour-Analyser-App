package com.example.behave;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etMobile, etAge, etPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        etPassword = findViewById(R.id.etPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> registerUser());
        tvGoToLogin.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(mobile) || TextUtils.isEmpty(age) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() != 6 || !TextUtils.isDigitsOnly(password)) {
            etPassword.setError("Password must be a 6-digit number");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration success, create user document in Firestore
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("email", email);
                            userData.put("mobile", mobile);
                            userData.put("age", age);
                            userData.put("createdAt", Timestamp.now());

                            db.collection("users").document(userId)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "User document created for " + userId);

                                        // Start detection service
                                        PrefManager prefManager = new PrefManager(RegisterActivity.this);
                                        prefManager.setDetectionActive(true);
                                        startService(new Intent(RegisterActivity.this, SpeechService.class));

                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error creating user document", e);
                                        // Even if document creation fails, proceed to MainActivity
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    });
                        }
                    } else {
                        // If registration fails, provide specific feedback
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        try {
                            throw task.getException();
                        } catch(FirebaseAuthWeakPasswordException e) {
                            etPassword.setError("Password is too weak.");
                        } catch(FirebaseAuthUserCollisionException e) {
                            etEmail.setError("This email is already in use.");
                        } catch(Exception e) {
                            // This will now catch the 'internal error' and show it to the user.
                            Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
