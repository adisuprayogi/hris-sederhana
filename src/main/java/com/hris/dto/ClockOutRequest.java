package com.hris.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Clock Out Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClockOutRequest {
    private Long employeeId;
    private LocalDateTime clockOutDateTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String deviceInfo;
    private String photoPath;
}
