package com.credshield.credshield.dto;

public class DashboardSummaryResponse {

    private long totalLoans;
    private long pendingLoans;
    private long approvedLoans;
    private long rejectedLoans;
    private long closedLoans;

    public DashboardSummaryResponse(
            long totalLoans,
            long pendingLoans,
            long approvedLoans,
            long rejectedLoans,
            long closedLoans) {

        this.totalLoans = totalLoans;
        this.pendingLoans = pendingLoans;
        this.approvedLoans = approvedLoans;
        this.rejectedLoans = rejectedLoans;
        this.closedLoans = closedLoans;
    }

    public long getTotalLoans() {
        return totalLoans;
    }

    public long getPendingLoans() {
        return pendingLoans;
    }

    public long getApprovedLoans() {
        return approvedLoans;
    }

    public long getRejectedLoans() {
        return rejectedLoans;
    }

    public long getClosedLoans() {
        return closedLoans;
    }
}