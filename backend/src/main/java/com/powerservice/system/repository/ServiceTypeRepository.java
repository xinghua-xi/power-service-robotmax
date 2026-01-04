package com.powerservice.system.repository;
import java.util.Optional;
import com.powerservice.system.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {
    List<ServiceType> findByIsActiveTrueOrderBySortOrderAsc();
    Optional<ServiceType> findByName(String name);
}
