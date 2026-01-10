package com.example.behave;

import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class Report implements Serializable {
    private String reportName;
    private Map<String, Double> summary;
    private Date createdAt;

    public Report() {
        // Needed for Firestore
    }

    public Report(String reportName, Map<String, Double> summary) {
        this.reportName = reportName;
        this.summary = summary;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public Map<String, Double> getSummary() {
        return summary;
    }

    public void setSummary(Map<String, Double> summary) {
        this.summary = summary;
    }

    @ServerTimestamp
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
