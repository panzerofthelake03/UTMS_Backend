package com.utms.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStudentId(Long studentId);
    List<Application> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<Application> findByStatus(String status);
    List<Application> findByStatusInOrderByCreatedAtAsc(Collection<String> statuses);
    List<Application> findByStatusOrderByCreatedAtAsc(String status);
    Optional<Application> findTopByStudentIdOrderByCreatedAtDesc(Long studentId);
    long countByStudentId(Long studentId);
    boolean existsByStudentIdAndStatus(Long studentId, String status);
}
