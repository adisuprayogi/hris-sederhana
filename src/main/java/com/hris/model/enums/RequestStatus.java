package com.hris.model.enums;

import lombok.Getter;

/**
 * Request Status Enum
 * Shared status for all 2-level approval requests (WFH, Overtime, Leave)
 */
@Getter
public enum RequestStatus {
    PENDING_SUPERVISOR("Menunggu Approval Atasan"),
    PENDING_HR("Menunggu Approval HR"),
    APPROVED("Disetujui"),
    REJECTED_BY_SUPERVISOR("Ditolak Atasan"),
    REJECTED_BY_HR("Ditolak HR");

    private final String displayName;

    RequestStatus(String displayName) {
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

    public boolean isPendingSupervisor() {
        return this == PENDING_SUPERVISOR;
    }

    public boolean isPendingHr() {
        return this == PENDING_HR;
    }
}
