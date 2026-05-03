package com.utms.intibak;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseExemptionRepository extends JpaRepository<CourseExemption, Long> {
    List<CourseExemption> findByApplicationIdOrderByStudentCourseCodeAsc(Long applicationId);
    long countByApplicationId(Long applicationId);
}
