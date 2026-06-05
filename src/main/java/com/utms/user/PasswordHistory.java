package com.utms.user;

import com.utms.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * One historical BCrypt password hash for a user.
 *
 * <p>Supports UC 1.4 Special Requirement #4 (History Check) and Alternative Course 4c: a newly
 * chosen password must not match any of the user's last 3 passwords.</p>
 */
@Entity
@Table(name = "password_history")
public class PasswordHistory extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    public PasswordHistory() {
    }

    public PasswordHistory(Long userId, String passwordHash) {
        this.userId = userId;
        this.passwordHash = passwordHash;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
