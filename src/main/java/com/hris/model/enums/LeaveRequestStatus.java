package com.hris.model.enums;

import lombok.Getter;

/**
 * Leave Request Status Enum
 * Supports 2-level approval workflow
 */
@Getter
public enum LeaveRequestStatus {
    PENDING_SUPERVISOR("Menunggu Approval Atasan"),
    PENDING_HR("Menunggu Approval HR"),
    APPROVED("Disetujui"),
    REJECTED_BY_SUPERVISOR("Ditolak Atasan"),
    REJECTED_BY_HR("Ditolak HR");

    private final String displayName;

    LeaveRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean isPending() {
        return this == PENDING_SUPERVISOR || this == PENDING_HR;
    }

    public boolean isApproved() {
        return this == APPROVED;
    }

    public boolean isRejected() {
        return this == REJECTED_BY_SUPERVISOR || this == REJECTED_BY_HR;
    }
}
