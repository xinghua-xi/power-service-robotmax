package com.powerservice.system.controller;

import com.powerservice.system.dto.ApiResponse;
import com.powerservice.system.entity.User;
import com.powerservice.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 获取所有用户（支持分页）
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findAll(pageable);
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", userPage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 根据ID查询用户
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("获取用户成功", user.get()));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("用户不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 更新用户
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("用户不存在"));
            }

            User user = userOptional.get();

            if (updates.containsKey("realName")) {
                user.setRealName((String) updates.get("realName"));
            }
            if (updates.containsKey("phone")) {
                user.setPhone((String) updates.get("phone"));
            }
            if (updates.containsKey("email")) {
                user.setEmail((String) updates.get("email"));
            }
            if (updates.containsKey("role")) {
                user.setRole((String) updates.get("role"));
            }

            userRepository.save(user);
            return ResponseEntity.ok(ApiResponse.success("更新用户成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                return ResponseEntity.ok(ApiResponse.success("删除用户成功", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("用户不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 根据用户名查询用户（适配 Optional<User> 类型）
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<User>> getUserByUsername(@PathVariable String username) {
        try {
            // 核心修正：接收 Optional<User> 类型返回值
            Optional<User> userOptional = userRepository.findByUsername(username);

            // 判断是否存在用户，避免空指针
            if (userOptional.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("查询用户成功", userOptional.get()));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("用户不存在"));
            }
        } catch (Exception e) {
            // 捕获并返回异常信息，便于调试
            return ResponseEntity.badRequest().body(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }
}