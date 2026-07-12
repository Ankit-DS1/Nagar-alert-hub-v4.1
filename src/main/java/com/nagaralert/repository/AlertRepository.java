package com.nagaralert.repository;

import com.nagaralert.model.Alert;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends MongoRepository<Alert, String> {
    List<Alert> findByLocation(String location);
    List<Alert> findByDepartment(String department);
    List<Alert> findByTimestampBefore(LocalDateTime timestamp);
    List<Alert> findByPhoneNumber(String phoneNumber);
    boolean existsByDescription(String description);
    List<Alert> findByStatusAndTimestampBefore(com.nagaralert.model.AlertStatus status, LocalDateTime timestamp);
}
