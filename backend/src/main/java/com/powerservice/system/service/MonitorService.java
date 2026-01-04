package com.powerservice.system.service;

import com.powerservice.system.entity.ElectricityData;
import com.powerservice.system.repository.ElectricityDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MonitorService {

    @Autowired
    private ElectricityDataRepository electricityDataRepository;

    public Map<String, Object> getMonitorData() {
        Map<String, Object> result = new HashMap<>();

        // 一次性获取所有需要的数据，减少数据库查询次数
        List<ElectricityData> allData = electricityDataRepository.findByPeriodDateBetween(
                LocalDate.now().minusYears(1), LocalDate.now());

        // 构建居民用电数据
        Map<String, Object> resident = buildElectricityData(allData, "resident");
        // 构建非居民用电数据
        Map<String, Object> nonResident = buildElectricityData(allData, "non_resident");

        result.put("resident", resident);
        result.put("nonResident", nonResident);
        result.put("currentDate", LocalDate.now().toString());
        result.put("systemStatus", "在线");

        return result;
    }

    /**
     * 根据数据类型和周期构建用电数据
     * @param allData 所有用电数据
     * @param dataType 数据类型（resident/non_resident）
     * @return 构建好的用电数据
     */
    private Map<String, Object> buildElectricityData(List<ElectricityData> allData, String dataType) {
        Map<String, Object> result = new HashMap<>();
        BigDecimal defaultAmount = dataType.equals("resident") ? new BigDecimal("0.17") : new BigDecimal("1.14");
        int defaultCount = dataType.equals("resident") ? 6 : 1;

        // 按周期分组
        Map<String, List<ElectricityData>> dataByPeriod = allData.stream()
                .filter(data -> dataType.equals(data.getDataType()))
                .collect(Collectors.groupingBy(ElectricityData::getPeriod));

        // 处理日数据
        processPeriodData(dataByPeriod, "day", result, defaultAmount, defaultCount);
        // 处理月数据
        processPeriodData(dataByPeriod, "month", result, defaultAmount, defaultCount);
        // 处理年数据
        processPeriodData(dataByPeriod, "year", result, defaultAmount, defaultCount);

        return result;
    }

    /**
     * 处理特定周期的数据
     * @param dataByPeriod 按周期分组的数据
     * @param period 周期（day/month/year）
     * @param result 结果映射
     * @param defaultAmount 默认金额
     * @param defaultCount 默认数量
     */
    private void processPeriodData(Map<String, List<ElectricityData>> dataByPeriod, String period, 
                                 Map<String, Object> result, BigDecimal defaultAmount, int defaultCount) {
        List<ElectricityData> periodData = dataByPeriod.getOrDefault(period, List.of());
        if (!periodData.isEmpty()) {
            // 获取最新的数据
            ElectricityData latestData = periodData.stream()
                    .max((d1, d2) -> d1.getPeriodDate().compareTo(d2.getPeriodDate()))
                    .orElse(null);
            if (latestData != null) {
                result.put(period + "Amount", latestData.getAmount());
                result.put(period + "Count", latestData.getCount());
                return;
            }
        }
        // 如果没有数据，使用默认值
        result.put(period + "Amount", defaultAmount);
        result.put(period + "Count", defaultCount);
    }

    public void updateElectricityData(String dataType, String period, LocalDate periodDate,
                                      BigDecimal amount, Integer count) {
        ElectricityData data = electricityDataRepository
                .findByDataTypeAndPeriodAndPeriodDate(dataType, period, periodDate)
                .orElse(new ElectricityData());

        data.setDataType(dataType);
        data.setPeriod(period);
        data.setPeriodDate(periodDate);
        data.setAmount(amount);
        data.setCount(count);
        data.setCategory(getCategoryByPeriod(period));

        electricityDataRepository.save(data);
    }

    private String getCategoryByPeriod(String period) {
        return switch (period) {
            case "day" -> "当日";
            case "month" -> "当月";
            case "year" -> "本年";
            default -> period;
        };
    }
}
