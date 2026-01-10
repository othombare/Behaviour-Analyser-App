package com.example.behave;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DashboardActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private TextView tvName, tvMobile, tvEmail, tvAge;
    private ImageView imgProfile;
    private Button btnEditProfile;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        getWindow().setStatusBarColor(
            ContextCompat.getColor(this, R.color.toolbar_blue)
        );

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        setupToolbarAndDrawer(R.id.toolbar, R.id.drawer_layout, R.id.nav_view);
        getSupportActionBar().setTitle("My Profile");

        tvName = findViewById(R.id.tvName);
        tvMobile = findViewById(R.id.tvMobile);
        tvEmail = findViewById(R.id.tvEmail);
        tvAge = findViewById(R.id.tvAge);
        imgProfile = findViewById(R.id.imgProfile);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        imgProfile.setOnClickListener(v -> openFileChooser());
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                StorageReference fileReference = storageRef.child(userId);

                fileReference.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            db.collection("users").document(userId).update("profileImageUrl", downloadUrl)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(DashboardActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                        Glide.with(this).load(downloadUrl).into(imgProfile);
                                    });
                        }))
                        .addOnFailureListener(e -> Toast.makeText(DashboardActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }
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
                            String email = documentSnapshot.getString("email");
                            String age = documentSnapshot.getString("age");
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            tvName.setText(name);
                            tvMobile.setText(mobile);
                            tvEmail.setText(email);
                            tvAge.setText(age);

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(this).load(profileImageUrl).into(imgProfile);
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
