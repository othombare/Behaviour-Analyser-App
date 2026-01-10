package com.example.behave;

import android.content.Context;
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

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private List<Report> reportList;
    private Context context;

    public ReportAdapter(Context context, List<Report> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.report_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Report report = reportList.get(position);
        holder.reportName.setText(report.getReportName());

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        holder.reportDate.setText(sdf.format(report.getCreatedAt()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportDetailActivity.class);
            intent.putExtra("report", report);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView reportName;
        TextView reportDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            reportName = itemView.findViewById(R.id.report_name);
            reportDate = itemView.findViewById(R.id.report_date);
        }
    }
}
