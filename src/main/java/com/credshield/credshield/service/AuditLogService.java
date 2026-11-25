
package com.credshield.credshield.service;

import com.credshield.credshield.entity.AuditLog;
import com.credshield.credshield.repository.AuditLogRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(
            String action,
            String performedBy,
            String details) {

        AuditLog auditLog = new AuditLog();

        auditLog.setAction(action);
        auditLog.setPerformedBy(performedBy);
        auditLog.setDetails(details);

        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllAuditLogs() {

        return auditLogRepository
                .findAllByOrderByCreatedAtDesc();
    }
}
