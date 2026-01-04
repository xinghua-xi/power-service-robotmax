package com.powerservice.system.controller;

import com.powerservice.system.dto.ApiResponse;
import com.powerservice.system.dto.KnowledgeBaseDto;
import com.powerservice.system.dto.ServiceTypeDto;
import com.powerservice.system.entity.KnowledgeBase;
import com.powerservice.system.entity.ServiceType;
import com.powerservice.system.repository.KnowledgeBaseRepository;
import com.powerservice.system.repository.ServiceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class KnowledgeBaseController {

    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    /**
     * 获取知识库列表
     */
    @GetMapping("/knowledge-base")
    public ResponseEntity<ApiResponse<List<KnowledgeBaseDto>>> getKnowledgeBaseList() {
        try {
            List<KnowledgeBase> knowledgeBaseList = knowledgeBaseRepository.findAll();
            List<KnowledgeBaseDto> knowledgeBaseDtos = knowledgeBaseList.stream()
                    .map(this::convertToKnowledgeBaseDto)
                    .collect(Collectors.toList());
            ApiResponse<List<KnowledgeBaseDto>> response = ApiResponse.success("获取知识库列表成功", knowledgeBaseDtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<KnowledgeBaseDto>> response = ApiResponse.error("获取知识库列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取服务类型列表
     */
    @GetMapping("/service-types")
    public ResponseEntity<ApiResponse<List<ServiceTypeDto>>> getServiceTypes() {
        try {
            List<ServiceType> serviceTypes = serviceTypeRepository.findByIsActiveTrueOrderBySortOrderAsc();
            List<ServiceTypeDto> serviceTypeDtos = serviceTypes.stream()
                    .map(this::convertToServiceTypeDto)
                    .collect(Collectors.toList());
            ApiResponse<List<ServiceTypeDto>> response = ApiResponse.success("获取服务类型列表成功", serviceTypeDtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<ServiceTypeDto>> response = ApiResponse.error("获取服务类型列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取热门问题列表
     */
    @GetMapping("/knowledge-base/popular")
    public ResponseEntity<ApiResponse<List<KnowledgeBaseDto>>> getPopularQuestions() {
        try {
            List<KnowledgeBase> popularQuestions = knowledgeBaseRepository.findPopularQuestions();
            List<KnowledgeBaseDto> popularQuestionDtos = popularQuestions.stream()
                    .map(this::convertToKnowledgeBaseDto)
                    .collect(Collectors.toList());
            ApiResponse<List<KnowledgeBaseDto>> response = ApiResponse.success("获取热门问题列表成功", popularQuestionDtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<KnowledgeBaseDto>> response = ApiResponse.error("获取热门问题列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据服务类型获取知识库
     */
    @GetMapping("/knowledge-base/service-type/{serviceTypeId}")
    public ResponseEntity<ApiResponse<List<KnowledgeBaseDto>>> getKnowledgeByServiceType(@PathVariable Long serviceTypeId) {
        try {
            List<KnowledgeBase> knowledgeBaseList = knowledgeBaseRepository.findByServiceTypeIdAndIsActiveTrue(serviceTypeId);
            List<KnowledgeBaseDto> knowledgeBaseDtos = knowledgeBaseList.stream()
                    .map(this::convertToKnowledgeBaseDto)
                    .collect(Collectors.toList());
            ApiResponse<List<KnowledgeBaseDto>> response = ApiResponse.success("根据服务类型获取知识库成功", knowledgeBaseDtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<KnowledgeBaseDto>> response = ApiResponse.error("根据服务类型获取知识库失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 将 ServiceType 转换为 ServiceTypeDto
    private ServiceTypeDto convertToServiceTypeDto(ServiceType serviceType) {
        ServiceTypeDto dto = new ServiceTypeDto();
        dto.setId(serviceType.getId());
        dto.setName(serviceType.getName());
        dto.setDescription(serviceType.getDescription());
        dto.setIcon(serviceType.getIcon());
        dto.setSortOrder(serviceType.getSortOrder());
        dto.setIsActive(serviceType.getIsActive());
        dto.setCreatedAt(serviceType.getCreatedAt());
        dto.setUpdatedAt(serviceType.getUpdatedAt());
        return dto;
    }

    // 将 KnowledgeBase 转换为 KnowledgeBaseDto
    private KnowledgeBaseDto convertToKnowledgeBaseDto(KnowledgeBase knowledgeBase) {
        KnowledgeBaseDto dto = new KnowledgeBaseDto();
        dto.setId(knowledgeBase.getId());
        dto.setQuestion(knowledgeBase.getQuestion());
        dto.setAnswer(knowledgeBase.getAnswer());
        
        // 只转换 ServiceType 的基本信息，避免懒加载问题
        if (knowledgeBase.getServiceType() != null) {
            ServiceTypeDto serviceTypeDto = convertToServiceTypeDto(knowledgeBase.getServiceType());
            dto.setServiceType(serviceTypeDto);
        }
        
        dto.setKeywords(knowledgeBase.getKeywords());
        dto.setHitCount(knowledgeBase.getHitCount());
        dto.setIsActive(knowledgeBase.getIsActive());
        dto.setCreatedAt(knowledgeBase.getCreatedAt());
        dto.setUpdatedAt(knowledgeBase.getUpdatedAt());
        return dto;
    }
}