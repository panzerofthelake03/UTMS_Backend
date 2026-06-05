package com.utms.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    /**
     * Returns this user's most recent password hashes, newest first. Pass a {@code Pageable} of
     * size N to fetch the last N entries (UC 1.4 history check uses N = 3).
     */
    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
