package com.example.behave;

import android.os.Bundle;
import androidx.core.content.ContextCompat;

public class SupportActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        getWindow().setStatusBarColor(
            ContextCompat.getColor(this, R.color.toolbar_blue)
        );

        setupToolbarAndDrawer(R.id.toolbar, R.id.drawer_layout, R.id.nav_view);
        getSupportActionBar().setTitle("Support");
    }
}
