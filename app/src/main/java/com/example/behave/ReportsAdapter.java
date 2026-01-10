package com.example.behave;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    private List<Report> reportList;

    public ReportsAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);
        holder.reportNameTextView.setText(report.getReportName());
        if (report.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            holder.reportDateTextView.setText(sdf.format(report.getCreatedAt()));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ReportDetailActivity.class);
            intent.putExtra("report", report);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {

        TextView reportNameTextView;
        TextView reportDateTextView;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reportNameTextView = itemView.findViewById(R.id.report_name_text_view);
            reportDateTextView = itemView.findViewById(R.id.report_date_text_view);
        }
    }
}
