package com.powerservice.system.controller;

import com.powerservice.system.dto.ApiResponse;
import com.powerservice.system.entity.ServiceType;
import com.powerservice.system.service.ChatService;
import com.powerservice.system.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ServiceController {

    @Autowired
    private com.powerservice.system.repository.ServiceTypeRepository serviceTypeRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private MonitorService monitorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceType>>> getAllServices() {
        try {
            List<ServiceType> services = serviceTypeRepository.findByIsActiveTrueOrderBySortOrderAsc();
            return ResponseEntity.ok(ApiResponse.success("获取服务列表成功", services));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceType>> getServiceById(@PathVariable Long id) {
        try {
            var service = serviceTypeRepository.findById(id);
            if (service.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("获取服务成功", service.get()));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("服务不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/monitor")
    public ResponseEntity<ApiResponse<Object>> getMonitorData() {
        try {
            var data = monitorService.getMonitorData();
            return ResponseEntity.ok(ApiResponse.success("获取监控数据成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
