package com.powerservice.system.controller;

import com.powerservice.system.dto.ApiResponse;
import com.powerservice.system.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MonitorController {

    @Autowired
    private MonitorService monitorService;

    @GetMapping("/electricity")
    public ResponseEntity<ApiResponse<Object>> getElectricityData() {
        try {
            var data = monitorService.getMonitorData();
            return ResponseEntity.ok(ApiResponse.success("获取电费数据成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/update-electricity")
    public ResponseEntity<ApiResponse<Object>> updateElectricityData(@RequestBody Map<String, Object> request) {
        try {
            // 支持蛇形命名法的参数
            String dataType = (String) request.get("data_type");
            String period = (String) request.get("period");
            String periodDateStr = (String) request.get("period_date");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            Integer count = (Integer) request.get("count");

            LocalDate periodDate = LocalDate.parse(periodDateStr);
            monitorService.updateElectricityData(dataType, period, periodDate, amount, count);

            return ResponseEntity.ok(ApiResponse.success("更新电费数据成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/system-status")
    public ResponseEntity<ApiResponse<Object>> getSystemStatus() {
        try {
            Map<String, Object> status = Map.of(
                    "status", "在线",
                    "timestamp", LocalDate.now().toString(),
                    "version", "1.0.0",
                    "usersOnline", 156
            );
            return ResponseEntity.ok(ApiResponse.success("获取系统状态成功", status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
