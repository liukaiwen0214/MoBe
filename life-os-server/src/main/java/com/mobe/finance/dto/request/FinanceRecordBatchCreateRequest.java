package com.mobe.finance.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class FinanceRecordBatchCreateRequest {

    @NotEmpty(message = "导入数据不能为空")
    @Valid
    private List<FinanceRecordCreateRequest> records;
}