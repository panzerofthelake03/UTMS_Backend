package com.utms.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);
    Optional<Document> findByIdAndApplicationId(Long id, Long applicationId);
    long countByApplicationId(Long applicationId);
}
