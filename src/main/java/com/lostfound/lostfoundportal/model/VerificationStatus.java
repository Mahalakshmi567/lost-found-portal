package com.lostfound.lostfoundportal.model;

/**
 * The admin verification pipeline a FOUND item moves through before it's
 * visible on the public dashboard. Only applies to FOUND items - LOST items
 * leave Item.verificationStatus null and are unaffected.
 *
 *   SUBMITTED -> PENDING_VERIFICATION -> ADMIN_REVIEW -> APPROVED
 */
public enum VerificationStatus {
    SUBMITTED,
    PENDING_VERIFICATION,
    ADMIN_REVIEW,
    APPROVED
}