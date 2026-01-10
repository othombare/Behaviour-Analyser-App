package com.example.behave;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected FirebaseAuth mAuth;
    protected FirebaseFirestore db;
    protected NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    protected void setupToolbarAndDrawer(int toolbarId, int drawerLayoutId, int navViewId) {
        Toolbar toolbar = findViewById(toolbarId);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(drawerLayoutId);
        navigationView = findViewById(navViewId);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        updateNavHeader();
    }

    protected void updateNavHeader() {
        if (navigationView == null) return;
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return; // Header not inflated
        TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
        ImageView navHeaderImage = headerView.findViewById(R.id.nav_header_image);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (navHeaderName != null) {
                                navHeaderName.setText(name);
                            }
                            // In a real app, you would load the user's profile image here
                        }
                    });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_my_profile) {
            if (!(this instanceof DashboardActivity)) {
                startActivity(new Intent(this, DashboardActivity.class));
            }
        } else if (id == R.id.nav_speech_logs) {
            if (!(this instanceof SpeechLogsActivity)) {
                startActivity(new Intent(this, SpeechLogsActivity.class));
            }
        } else if (id == R.id.nav_reports) {
            if (!(this instanceof ReportsActivity)) {
                startActivity(new Intent(this, ReportsActivity.class));
            }
        } else if (id == R.id.nav_settings) {
            if (!(this instanceof SettingsActivity)) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
        } else if (id == R.id.nav_privacy) {
            if (!(this instanceof PrivacyActivity)) {
                startActivity(new Intent(this, PrivacyActivity.class));
            }
        } else if (id == R.id.nav_about) {
            if (!(this instanceof AboutActivity)) {
                startActivity(new Intent(this, AboutActivity.class));
            }
        } else if (id == R.id.nav_support) {
            if (!(this instanceof SupportActivity)) {
                startActivity(new Intent(this, SupportActivity.class));
            }
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (this instanceof MainActivity) {
                super.onBackPressed();
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }
}
