package com.example.behave;

import android.os.Bundle;
import androidx.core.content.ContextCompat;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getWindow().setStatusBarColor(
            ContextCompat.getColor(this, R.color.toolbar_blue)
        );

        setupToolbarAndDrawer(R.id.toolbar, R.id.drawer_layout, R.id.nav_view);
        getSupportActionBar().setTitle("Settings");
    }
}
