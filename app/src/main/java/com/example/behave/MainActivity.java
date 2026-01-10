package com.example.behave;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity {

    private static final int RECORD_AUDIO_PERMISSION_CODE = 1;
    private static final int POST_NOTIFICATIONS_PERMISSION_CODE = 2;
    private static final String TAG = "MainActivity";
    PrefManager pref;
    SwitchCompat toggleServiceSwitch;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private FirebaseAuth.AuthStateListener authStateListener;
    private PieChart mainPieChart;
    private CardView pieChartCard;
    private ListenerRegistration reportListener;
    private static final String REPORT_CHANNEL_ID = "report_notification_channel";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(
            ContextCompat.getColor(this, R.color.toolbar_blue)
        );

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupAuthStateListener();

        setupToolbarAndDrawer(R.id.toolbar, R.id.drawer_layout, R.id.nav_view);
        getSupportActionBar().setTitle("Main Activity");

        drawerLayout = findViewById(R.id.drawer_layout);

        pref = new PrefManager(this);
        toggleServiceSwitch = findViewById(R.id.swipeDetect);
        mainPieChart = findViewById(R.id.main_pie_chart);
        pieChartCard = findViewById(R.id.pie_chart_card);
        Button viewReportsButton = findViewById(R.id.view_reports_button);

        // Set the button color programmatically
        int colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);
        viewReportsButton.setBackgroundTintList(ColorStateList.valueOf(colorPrimary));

        viewReportsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportsActivity.class);
            startActivity(intent);
        });

        if (pref.isDetectionActive()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startSpeechService();
            } else {
                pref.setDetectionActive(false);
            }
        }
        updateSwitchState();

        toggleServiceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
                } else {
                    startServiceAndShowToast();
                }
            } else {
                stopServiceAndShowToast();
            }
        });
        loadUserProfile();
        createNotificationChannel();
        requestNotificationPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_my_profile) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
            if (pref.isDetectionActive()) {
                stopServiceAndShowToast();
            }
            mAuth.signOut();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateSwitchState();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void startServiceAndShowToast() {
        pref.setDetectionActive(true);
        startSpeechService();
        Toast.makeText(this,"Detection Started",Toast.LENGTH_SHORT).show();
    }

    private void stopServiceAndShowToast() {
        pref.setDetectionActive(false);
        stopSpeechService();
        Toast.makeText(this,"Detection Stopped",Toast.LENGTH_SHORT).show();
    }

    private void updateSwitchState() {
        toggleServiceSwitch.setChecked(pref.isDetectionActive());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startServiceAndShowToast();
            } else {
                Toast.makeText(this, "Permission to record audio is required for this feature.", Toast.LENGTH_SHORT).show();
                pref.setDetectionActive(false);
                updateSwitchState();
            }
        } else if (requestCode == POST_NOTIFICATIONS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listenForLatestReport();
            } else {
                Toast.makeText(this, "Permission to post notifications is required to receive report alerts.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startSpeechService() {
        Intent serviceIntent = new Intent(this, SpeechService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopSpeechService() {
        Intent stopIntent = new Intent(this, SpeechService.class);
        stopService(stopIntent);
    }

    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // User is signed out
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        };
    }

    private void listenForLatestReport() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        reportListener = db.collection("users").document(userId).collection("reports")
                .orderBy("createdAt", Query.Direction.DESCENDING).limit(1)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        pieChartCard.setVisibility(View.GONE);
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            sendReportNotification();
                            DocumentSnapshot document = dc.getDocument();
                            Report report = document.toObject(Report.class);
                            if (report != null && report.getSummary() != null && !report.getSummary().isEmpty()) {
                                displayReport(report);
                                pieChartCard.setVisibility(View.VISIBLE);
                            } else {
                                pieChartCard.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

    private void sendReportNotification() {
        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Intent intent = new Intent(this, ReportsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, REPORT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_report)
                    .setContentTitle("New Report Generated")
                    .setContentText("A new report has been generated for you.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Report Notifications";
            String description = "Notifications for new reports";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(REPORT_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        POST_NOTIFICATIONS_PERMISSION_CODE);
            }
        }
    }


    private void displayReport(Report report) {
        setupPieChart(report.getSummary());
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            NavigationView navigationView = findViewById(R.id.nav_view);
                            View headerView = navigationView.getHeaderView(0);
                            TextView navUsername = headerView.findViewById(R.id.nav_header_name);
                            navUsername.setText(name);
                        }
                    });
        }
    }

    private void setupPieChart(Map<String, Double> summary) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : summary.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.5f);
        dataSet.setValueLinePart2Length(0.5f);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(mainPieChart));
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.BLACK);

        mainPieChart.setData(pieData);

        mainPieChart.setUsePercentValues(true);
        mainPieChart.getDescription().setEnabled(false);
        mainPieChart.setExtraOffsets(5, 10, 5, 10);
        mainPieChart.setDragDecelerationFrictionCoef(0.95f);

        mainPieChart.setDrawHoleEnabled(true);
        mainPieChart.setHoleColor(Color.WHITE);
        mainPieChart.setTransparentCircleColor(Color.WHITE);
        mainPieChart.setTransparentCircleAlpha(110);
        mainPieChart.setHoleRadius(50f);
        mainPieChart.setTransparentCircleRadius(55f);

        mainPieChart.setDrawCenterText(true);
        mainPieChart.setCenterText("Latest Summary");
        mainPieChart.setCenterTextSize(14f);


        mainPieChart.setRotationAngle(0);
        mainPieChart.setRotationEnabled(true);
        mainPieChart.setHighlightPerTapEnabled(true);
        mainPieChart.animateY(1400);

        mainPieChart.setDrawEntryLabels(false);
        
        Legend l = mainPieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setWordWrapEnabled(true);
        l.setEnabled(true);

        mainPieChart.invalidate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            listenForLatestReport();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
        if (reportListener != null) {
            reportListener.remove();
        }
    }
}
