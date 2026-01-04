package com.powerservice.system.repository;

import com.powerservice.system.entity.ElectricityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ElectricityDataRepository extends JpaRepository<ElectricityData, Long> {
    List<ElectricityData> findByDataTypeAndPeriodOrderByPeriodDateDesc(String dataType, String period);
    Optional<ElectricityData> findByDataTypeAndPeriodAndPeriodDate(String dataType, String period, LocalDate periodDate);
    List<ElectricityData> findByPeriodDateBetween(LocalDate startDate, LocalDate endDate);
}