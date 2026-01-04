package com.powerservice.system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ElectricityUpdateRequest {
    @NotNull(message = "数据类型不能为空")
    @JsonProperty("data_type")
    private String dataType;
    
    @NotNull(message = "周期类型不能为空")
    private String period;
    
    @NotNull(message = "周期日期不能为空")
    @JsonProperty("period_date")
    private LocalDate periodDate;
    
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.00", message = "金额不能小于0")
    private BigDecimal amount;
    
    @NotNull(message = "数量不能为空")
    @Min(value = 0, message = "数量不能小于0")
    private Integer count;
    
    // getter and setter methods
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    public String getPeriod() {
        return period;
    }
    
    public void setPeriod(String period) {
        this.period = period;
    }
    
    public LocalDate getPeriodDate() {
        return periodDate;
    }
    
    public void setPeriodDate(LocalDate periodDate) {
        this.periodDate = periodDate;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
}