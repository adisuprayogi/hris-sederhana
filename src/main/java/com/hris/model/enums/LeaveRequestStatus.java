package com.hris.model.enums;

import lombok.Getter;

/**
 * Leave Request Status Enum
 */
@Getter
public enum LeaveRequestStatus {
    PENDING("Menunggu Approval"),
    APPROVED("Disetujui"),
    REJECTED("Ditolak");

    private final String displayName;

    LeaveRequestStatus(String displayName) {
        this.displayName = displayName;
    }
}
