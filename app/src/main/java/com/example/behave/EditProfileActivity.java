package com.example.behave;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends BaseActivity {

    private TextInputEditText etName, etMobile, etAge;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        getWindow().setStatusBarColor(
            ContextCompat.getColor(this, R.color.toolbar_blue)
        );

        setupToolbarAndDrawer(R.id.toolbar, R.id.drawer_layout, R.id.nav_view);
        getSupportActionBar().setTitle("Edit Profile");

        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        btnSave = findViewById(R.id.btnSave);

        loadUserProfile();

        btnSave.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String mobile = documentSnapshot.getString("mobile");
                            String age = documentSnapshot.getString("age");

                            etName.setText(name);
                            etMobile.setText(mobile);
                            etAge.setText(age);
                        }
                    });
        }
    }

    private void saveUserProfile() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String age = etAge.getText().toString().trim();

        if (name.isEmpty() || mobile.isEmpty() || age.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("name", name);
            updatedData.put("mobile", mobile);
            updatedData.put("age", age);

            db.collection("users").document(userId).update(updatedData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show());
        }
    }
}
