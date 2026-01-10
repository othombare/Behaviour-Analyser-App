package com.example.behave;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SpeechLogsActivity extends BaseActivity {

    private FirebaseFirestore db;
    private ListView speechLogsListView;
    private SimpleAdapter speechLogsAdapter;
    private List<Map<String, String>> speechLogItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_logs);

        getWindow().setStatusBarColor(
            ContextCompat.getColor(this, R.color.toolbar_blue)
        );

        db = FirebaseFirestore.getInstance();

        setupToolbarAndDrawer(R.id.toolbar, R.id.drawer_layout, R.id.nav_view);
        getSupportActionBar().setTitle("Speech Logs");

        speechLogsListView = findViewById(R.id.speechLogsListView);
        speechLogItems = new ArrayList<>();
        speechLogsAdapter = new SimpleAdapter(this, speechLogItems,
                R.layout.speech_log_item, new String[]{"log"}, new int[]{R.id.speechLogTextView});
        speechLogsListView.setAdapter(speechLogsAdapter);

        loadSpeechLogs();
    }

    private void loadSpeechLogs() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return; // No user logged in
        }
        String userId = currentUser.getUid();

        db.collection("users").document(userId).collection("speech_logs")
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(50) // Limiting to the last 50 entries for performance
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("SpeechLogsActivity", "Listen failed.", e);
                        return;
                    }

                    speechLogItems.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String text = doc.getString("text");
                        Timestamp timestamp = doc.getTimestamp("time");
                        if (text != null && timestamp != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            String formattedDate = sdf.format(timestamp.toDate());
                            Map<String, String> item = new HashMap<>();
                            item.put("log", formattedDate + ": " + text);
                            speechLogItems.add(item);
                        }
                    }
                    speechLogsAdapter.notifyDataSetChanged();
                });
    }
}
