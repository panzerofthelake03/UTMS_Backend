package com.utms.document;

/**
 * Simulated virus scan result statuses.
 * Mirrors the CHECK constraint in the documents table.
 */
public final class ScanStatus {
    public static final String PENDING  = "PENDING";
    public static final String CLEAN    = "CLEAN";
    public static final String INFECTED = "INFECTED";

    private ScanStatus() {}
}
