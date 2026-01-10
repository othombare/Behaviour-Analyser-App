package com.example.behave;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportDetailActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private PieChart pieChart;
    private ImageButton downloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        getWindow().setStatusBarColor(
            ContextCompat.getColor(this, R.color.toolbar_blue)
        );

        pieChart = findViewById(R.id.pie_chart);
        TextView noDataTextView = findViewById(R.id.no_data_textview);
        downloadButton = findViewById(R.id.download_button);

        Report report = (Report) getIntent().getSerializableExtra("report");

        PieDataSet dataSet = null;
        if (report != null && report.getSummary() != null) {
            dataSet = createDataSet(report.getSummary());
        }

        if (dataSet != null && dataSet.getEntryCount() > 0) {

            pieChart.setVisibility(View.VISIBLE);
            noDataTextView.setVisibility(View.GONE);
            downloadButton.setEnabled(true);

            pieChart.setUsePercentValues(true);
            pieChart.getDescription().setEnabled(false);
            pieChart.setCenterText("Behavioral Summary");
            pieChart.setCenterTextSize(15f);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleColor(Color.WHITE);
            pieChart.setHoleRadius(58f);
            pieChart.setTransparentCircleRadius(61f);
            pieChart.setDrawEntryLabels(false);
            pieChart.animateY(1400);

            Legend legend = pieChart.getLegend();
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setWordWrapEnabled(true);
            legend.setTextSize(15f);

            PieData pieData = new PieData(dataSet);
            pieData.setValueFormatter(new PercentFormatter(pieChart));
            pieData.setValueTextSize(17f);
            pieData.setValueTextColor(Color.BLACK);

            pieChart.setData(pieData);
            pieChart.invalidate();

        } else {
            pieChart.setVisibility(View.GONE);
            noDataTextView.setVisibility(View.VISIBLE);
            downloadButton.setEnabled(false);
        }

        downloadButton.setOnClickListener(v -> saveChart());
    }

    private PieDataSet createDataSet(Map<String, Double> summary) {
        List<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Double> entry : summary.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1Length(0.5f);
        dataSet.setValueLinePart2Length(0.5f);

        return dataSet;
    }

    private void saveChart() {
        if (pieChart.getVisibility() != View.VISIBLE || pieChart.getData() == null) {
            Toast.makeText(this, "No report to download", Toast.LENGTH_SHORT).show();
            return;
        }

        pieChart.invalidate();
        pieChart.post(() -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                } else {
                    saveChartToFile();
                }
            } else {
                saveChartToFile();
            }
        });
    }

    private void saveChartToFile() {
        Bitmap bitmap = pieChart.getChartBitmap();

        if (bitmap == null) {
            Toast.makeText(this, "Failed to save report", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "BehaveReport_" + System.currentTimeMillis() + ".png";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (uri == null) {
                    throw new IOException("Failed to create MediaStore entry.");
                }

                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    if (out == null) {
                        throw new IOException("Failed to open output stream.");
                    }
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }

            } else {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                // Ensure the directory exists.
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
                }
                File file = new File(dir, fileName);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }
            }

            Toast.makeText(this, "Report saved to Downloads", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_STORAGE_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            saveChartToFile();
        }
    }
}
