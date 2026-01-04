package com.powerservice.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

// 核心修改：移除外部Tomcat相关的继承和重写
@SpringBootApplication
public class PowerServiceApplication {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 保留main方法（内置Tomcat运行的核心入口）
    public static void main(String[] args) {
        SpringApplication.run(PowerServiceApplication.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        // 配置LocalDateTime序列化格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        module.addSerializer(new LocalDateTimeSerializer(formatter));
        mapper.registerModule(module);

        // 配置日期时间格式
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // 禁用将日期时间序列化为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 启用解析未知属性时忽略失败（可选）
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 禁用空bean序列化失败
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return mapper;
    }

    // 添加CommandLineRunner来执行SQL修复语句
    @Bean
    public CommandLineRunner fixChatRecords() {
        return args -> {
            try {
                // 更新所有null的bot_response为''
                jdbcTemplate.update("UPDATE chat_records SET bot_response = '' WHERE bot_response IS NULL");
                
                // 修改字段为NOT NULL约束
                jdbcTemplate.update("ALTER TABLE chat_records MODIFY COLUMN bot_response TEXT NOT NULL");
                
                System.out.println("成功修复chat_records表的bot_response字段");
            } catch (Exception e) {
                System.out.println("修复chat_records表时出现异常: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

}

