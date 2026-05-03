package com.utms.common.security;

import com.utms.application.Application;
import com.utms.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PermissionChecker {

    // -------------------------------------------------------
    // Role helpers
    // -------------------------------------------------------

    public boolean hasRole(String roleName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(roleName::equals);
    }

    public boolean isAdmin() {
        return hasRole(RoleConstants.ROLE_ADMIN);
    }

    public boolean isStudent() {
        return hasRole(RoleConstants.ROLE_STUDENT);
    }

    public boolean isOidb() {
        return hasRole(RoleConstants.ROLE_OIDB);
    }

    public boolean isYdyo() {
        return hasRole(RoleConstants.ROLE_YDYO);
    }

    public boolean isYgk() {
        return hasRole(RoleConstants.ROLE_YGK);
    }

    // -------------------------------------------------------
    // Ownership helpers
    // -------------------------------------------------------

    public boolean isOwner(User resourceOwner) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getName().equals(resourceOwner.getEmail());
    }

    // -------------------------------------------------------
    // Application-level permission checks
    // -------------------------------------------------------

    /**
     * A user can view an application if they are the owner, or have a staff role.
     */
    public boolean canViewApplication(User resourceOwner) {
        return isOwner(resourceOwner) || isOidb() || isYdyo() || isYgk() || isAdmin();
    }

    /**
     * OIDB can forward an application to YDYO when it is in SUBMITTED or UNDER_REVIEW status.
     */
    public boolean canForwardToYdyo(Application application) {
        if (!isOidb() && !isAdmin()) return false;
        String status = application.getStatus();
        return "SUBMITTED".equals(status) || "UNDER_REVIEW".equals(status);
    }

    /**
     * YDYO can review the English document when the application is under YDYO review.
     */
    public boolean canReviewEnglishDocument(Application application) {
        if (!isYdyo() && !isAdmin()) return false;
        return "UNDER_YDYO_REVIEW".equals(application.getStatus());
    }

    /**
     * YGK can evaluate and compute composite score.
     */
    public boolean canEvaluate(Application application) {
        if (!isYgk() && !isAdmin()) return false;
        return "UNDER_YGK_REVIEW".equals(application.getStatus());
    }

    /**
     * OIDB can publish the final result once YGK evaluation is complete.
     */
    public boolean canPublishResult(Application application) {
        if (!isOidb() && !isAdmin()) return false;
        return "UNDER_YGK_REVIEW".equals(application.getStatus())
                || "ACCEPTED".equals(application.getStatus())
                || "REJECTED".equals(application.getStatus());
    }
}
