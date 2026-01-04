package com.powerservice.system.repository;

import com.powerservice.system.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    @Query("SELECT kb FROM KnowledgeBase kb WHERE " +
            "(kb.question LIKE %:keyword% OR " +
            "kb.answer LIKE %:keyword% OR " +
            "kb.keywords LIKE %:keyword%) AND " +
            "kb.isActive = true")
    List<KnowledgeBase> findByKeyword(@Param("keyword") String keyword);

    List<KnowledgeBase> findByServiceTypeIdAndIsActiveTrue(Long serviceTypeId);

    @Query("SELECT kb FROM KnowledgeBase kb WHERE kb.isActive = true ORDER BY kb.hitCount DESC")
    List<KnowledgeBase> findPopularQuestions();
}
