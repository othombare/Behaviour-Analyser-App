package com.example.behave;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

public class NotificationsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        getWindow().setStatusBarColor(
            ContextCompat.getColor(this, R.color.toolbar_blue)
        );

        setupToolbarAndDrawer(R.id.toolbar, R.id.drawer_layout, R.id.nav_view);
        getSupportActionBar().setTitle(R.string.title_notifications);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_my_profile) {
            startActivity(new Intent(this, DashboardActivity.class));
        } else if (id == R.id.nav_speech_logs) {
            startActivity(new Intent(this, SpeechLogsActivity.class));
        } else if (id == R.id.nav_reports) {
            startActivity(new Intent(this, ReportsActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_privacy) {
            startActivity(new Intent(this, PrivacyActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.nav_support) {
            startActivity(new Intent(this, SupportActivity.class));
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
