package com.powerservice.system.repository;

import com.powerservice.system.entity.ChatRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatRecordRepository extends JpaRepository<ChatRecord, Long> {
    List<ChatRecord> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    Long countBySessionId(String sessionId);
    List<ChatRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}
