package com.utms.application;

public final class ApplicationStatus {

    private ApplicationStatus() {
    }

    public static final String DRAFT              = "DRAFT";
    public static final String SUBMITTED          = "SUBMITTED";
    public static final String UNDER_OIDB_REVIEW  = "UNDER_OIDB_REVIEW";
    public static final String UNDER_YDYO_REVIEW  = "UNDER_YDYO_REVIEW";
    public static final String UNDER_YGK_REVIEW   = "UNDER_YGK_REVIEW";
    public static final String ACCEPTED           = "ACCEPTED";
    public static final String REJECTED           = "REJECTED";
}
