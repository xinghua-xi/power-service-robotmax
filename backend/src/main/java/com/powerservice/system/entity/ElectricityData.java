package com.powerservice.system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "electricity_data")
public class ElectricityData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;

    @Column(nullable = false, length = 20)
    private String period;

    @Column(name = "period_date", nullable = false)
    private LocalDate periodDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Integer count;

    @Column(length = 50)
    private String category;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 1. 无参构造函数（JPA 持久化必需）
    public ElectricityData() {
    }

    // 2. 保留 JPA 自动初始化/更新时间的逻辑
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== Getter 方法（按属性逐一实现） ====================
    public Long getId() {
        return id;
    }

    public String getDataType() {
        return dataType;
    }

    public String getPeriod() {
        return period;
    }

    public LocalDate getPeriodDate() {
        return periodDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Integer getCount() {
        return count;
    }

    public String getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ==================== Setter 方法（按属性逐一实现） ====================
    public void setId(Long id) {
        this.id = id;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public void setPeriodDate(LocalDate periodDate) {
        this.periodDate = periodDate;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // createdAt 由 @PrePersist 自动初始化，仍保留 setter（兼容特殊场景）
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // updatedAt 由 @PreUpdate 自动更新，保留 setter（兼容手动更新）
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
