package com.utms.auth;

/**
 * Lifecycle states for a {@link PasswordResetRequest} (UC 1.3 + UC 1.4).
 */
public final class PasswordResetStatus {

    /** OTP issued, awaiting verification (UC 1.3, steps 4-7). */
    public static final String PENDING_OTP = "PENDING_OTP";

    /** OTP verified, a temporary reset token is active (UC 1.3 -> UC 1.4). */
    public static final String VERIFIED = "VERIFIED";

    /** New password successfully set; the reset token is consumed (UC 1.4 POST-2). */
    public static final String USED = "USED";

    /** OTP or reset token expired. */
    public static final String EXPIRED = "EXPIRED";

    /** Flow superseded (e.g. a new request) or abandoned. */
    public static final String CANCELED = "CANCELED";

    private PasswordResetStatus() {
    }
}
