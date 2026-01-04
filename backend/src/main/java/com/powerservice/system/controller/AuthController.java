package com.powerservice.system.controller;

import java.util.Map;
import com.powerservice.system.dto.ApiResponse;
import com.powerservice.system.dto.FaceLoginRequest;
import com.powerservice.system.dto.LoginRequest;
import com.powerservice.system.entity.User;
import com.powerservice.system.repository.UserRepository;
import com.powerservice.system.service.AuthService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            var result = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("登录成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("非法参数：" + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("系统错误：" + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("未知错误：" + e.getMessage()));
        }
    }

    @PostMapping({"/face-login", "/face_login"})
    public ResponseEntity<ApiResponse<Object>> faceLogin(@Valid @RequestBody FaceLoginRequest request) {
        try {
            var result = authService.faceLogin(request);
            return ResponseEntity.ok(ApiResponse.success("面容登录成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("非法参数：" + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("系统错误：" + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("未知错误：" + e.getMessage()));
        }
    }

    @PostMapping("/register-face/{userIdOrUsername}")
    public ResponseEntity<ApiResponse<Object>> registerFace(
            @PathVariable String userIdOrUsername,
            @RequestBody Map<String, String> request) {
        try {
            String faceData = request.get("faceData");
            if (faceData == null || faceData.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("缺少 faceData 参数"));
            }
            boolean success = authService.registerFace(userIdOrUsername, faceData);
            return ResponseEntity.ok(ApiResponse.success("面容注册成功", success));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("非法参数：" + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("系统错误：" + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("未知错误：" + e.getMessage()));
        }
    }

    @GetMapping({"/check-face-registered/{userIdOrUsername}", "/checkFaceRegistered/{userIdOrUsername}"})
    public ResponseEntity<ApiResponse<Object>> checkFaceRegistered(@PathVariable String userIdOrUsername) {
        try {
            Optional<User> userOptional;
            
            try {
                // 尝试将参数转换为Long类型的userId
                Long userId = Long.parseLong(userIdOrUsername);
                userOptional = userRepository.findById(userId);
            } catch (NumberFormatException e) {
                // 如果转换失败，使用参数作为username查找用户
                userOptional = userRepository.findByUsername(userIdOrUsername);
            }
            
            boolean faceRegistered = userOptional.map(User::getFaceRegistered).orElse(false);
            return ResponseEntity.ok(ApiResponse.success("查询成功", faceRegistered));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }
}

