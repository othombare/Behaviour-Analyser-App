package com.example.behave;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.FileProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReportActivity extends BaseActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = FirebaseFirestore.getInstance();

        setupToolbarAndDrawer(R.id.toolbar, R.id.drawer_layout, R.id.nav_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Report Detail");
        }

        String reportId = getIntent().getStringExtra("reportId");
        if (reportId != null && !reportId.isEmpty()) {
            loadReport(reportId);
        } else {
            Toast.makeText(this, "Error: Report ID not found.", Toast.LENGTH_LONG).show();
            Log.e("ReportActivity", "Report ID is null or empty.");
            finish(); // Close the activity if there's no report to show
        }
    }

    private void loadReport(String reportId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w("ReportActivity", "No user is signed in.");
            Toast.makeText(this, "Error: You are not signed in.", Toast.LENGTH_LONG).show();
            return;
        }
        String userId = currentUser.getUid();

        db.collection("users").document(userId).collection("reports").document(reportId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String base64Pdf = documentSnapshot.getString("pdf_base64");
                        if (base64Pdf != null && !base64Pdf.isEmpty()) {
                            openPdf(base64Pdf);
                        } else {
                            Log.e("ReportActivity", "PDF data is missing from the report document.");
                            Toast.makeText(this, "Error: Could not find PDF data for this report.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d("ReportActivity", "No such document for reportId: " + reportId);
                        Toast.makeText(this, "Error: Report not found.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ReportActivity", "Error getting document", e);
                    Toast.makeText(this, "Error loading report. Please try again.", Toast.LENGTH_LONG).show();
                });
    }

    private void openPdf(String base64Pdf) {
        try {
            byte[] pdfAsBytes = Base64.decode(base64Pdf, Base64.DEFAULT);
            File pdfFile = new File(getCacheDir(), "report.pdf");

            if (pdfFile.exists()) {
                if (!pdfFile.delete()) {
                    Log.w("ReportActivity", "Failed to delete existing report file");
                }
            }

            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                fos.write(pdfAsBytes);
                fos.flush();
            }

            if (!pdfFile.exists() || pdfFile.length() == 0) {
                 Log.e("ReportActivity", "PDF file was not created or is empty.");
                 Toast.makeText(this, "Error: Could not create PDF file.", Toast.LENGTH_LONG).show();
                 return;
            }

            Uri pdfUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", pdfFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivity(intent);
            finish();

        } catch (IllegalArgumentException e) {
            Log.e("ReportActivity", "Base64 decoding failed", e);
            Toast.makeText(this, "Error: Corrupted report data.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("ReportActivity", "Error writing PDF file", e);
            Toast.makeText(this, "Error opening PDF. Could not save file.", Toast.LENGTH_LONG).show();
        } catch (ActivityNotFoundException e) {
            Log.e("ReportActivity", "No PDF viewer installed", e);
            Toast.makeText(this, "No application available to view PDF", Toast.LENGTH_LONG).show();
        }
    }
}
