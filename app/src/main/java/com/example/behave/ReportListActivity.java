package com.example.behave;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ReportListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private List<Report> reportList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);

        getWindow().setStatusBarColor(
            ContextCompat.getColor(this, R.color.toolbar_blue)
        );

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reportList = new ArrayList<>();
        adapter = new ReportAdapter(this, reportList);
        recyclerView.setAdapter(adapter);

        loadReports();
    }

    private void loadReports() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("reports")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Report report = document.toObject(Report.class);
                            reportList.add(report);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
