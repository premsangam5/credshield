
package com.credshield.credshield.dto;

import com.credshield.credshield.entity.AuditLog;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private Long id;
    private String action;
    private String performedBy;
    private String details;
    private LocalDateTime createdAt;

    public AuditLogResponse(AuditLog auditLog) {
        this.id = auditLog.getId();
        this.action = auditLog.getAction();
        this.performedBy = auditLog.getPerformedBy();
        this.details = auditLog.getDetails();
        this.createdAt = auditLog.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
