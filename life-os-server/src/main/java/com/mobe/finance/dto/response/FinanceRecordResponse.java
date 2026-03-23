package com.mobe.finance.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class FinanceRecordResponse {

    private String id;
    private String type;
    private String category;
    private BigDecimal amount;
    private LocalDateTime recordDate;
    private String remark;
    private LocalDateTime createdAt;
}