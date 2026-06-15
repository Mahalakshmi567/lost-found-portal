package com.lostfound.lostfoundportal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
@Entity
public class ClaimRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
@NotBlank(message = "Claimant Name is required")
    private String claimantName;
@NotBlank(message = "Reason is required")
    @Column(length = 1000)
    private String reason;
@NotBlank(message = "Contact Number is required")
    private String contactNumber;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public ClaimRequest() {
    }

    public Long getId() {
        return id;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public void setClaimantName(String claimantName) {
        this.claimantName = claimantName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}