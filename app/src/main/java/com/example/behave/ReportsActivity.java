package com.example.behave;

import android.os.Bundle;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ReportsActivity extends BaseActivity {

    private static final String TAG = "ReportsActivity";
    private RecyclerView reportsRecyclerView;
    private ReportsAdapter reportsAdapter;
    private List<Report> reportList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        getWindow().setStatusBarColor(
            ContextCompat.getColor(this, R.color.toolbar_blue)
        );

        setupToolbarAndDrawer(R.id.toolbar, R.id.drawer_layout, R.id.nav_view);
        getSupportActionBar().setTitle("Reports");

        reportsRecyclerView = findViewById(R.id.reports_recycler_view);
        reportList = new ArrayList<>();
        reportsAdapter = new ReportsAdapter(reportList);
        reportsRecyclerView.setAdapter(reportsAdapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        fetchReports();
    }

    private void fetchReports() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("reports")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reportList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "Report data: " + document.getData());
                            Report report = document.toObject(Report.class);
                            reportList.add(report);
                        }
                        reportsAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }
}
