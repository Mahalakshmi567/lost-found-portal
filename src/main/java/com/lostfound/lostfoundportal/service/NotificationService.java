package com.lostfound.lostfoundportal.service;

import com.lostfound.lostfoundportal.model.ClaimRequest;
import com.lostfound.lostfoundportal.model.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Sends the two notification emails the app cares about:
 *  1. an item owner gets emailed when someone claims their item
 *  2. a lost-item owner gets emailed when a newly reported found item
 *     looks like a strong match
 *
 * Email is a nice-to-have, not core functionality - a failed send is
 * logged and swallowed rather than breaking the claim/report flow that
 * triggered it.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public NotificationService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromAddress) {

        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Async
    public void notifyItemClaimed(Item claimedItem, ClaimRequest claimRequest) {

        if (claimedItem.getUser() == null || isBlank(claimedItem.getUser().getEmail())) {
            return;
        }

        String subject = "Someone has claimed your item: " + claimedItem.getItemName();

        String body = """
                Hi %s,

                %s has submitted a claim on the item you posted: "%s".

                Claim details:
                - Reason given: %s
                - Contact number: %s

                Log in to the Lost & Found Portal and open My Posts > View Claims \
                on this item to see the full request.

                - Lost & Found Portal
                """.formatted(
                claimedItem.getUser().getName(),
                claimRequest.getClaimantName(),
                claimedItem.getItemName(),
                claimRequest.getReason(),
                claimRequest.getContactNumber());

        send(claimedItem.getUser().getEmail(), subject, body);
    }

    @Async
    public void notifyPossibleMatch(Item lostItem, Item foundItem, int score) {

        if (lostItem.getUser() == null || isBlank(lostItem.getUser().getEmail())) {
            return;
        }

        String subject = "Possible match found for your lost item: " + lostItem.getItemName();

        String body = """
                Hi %s,

                A newly reported found item looks like a %d%% match for something \
                you reported lost: "%s".

                Found item details:
                - Name: %s
                - Description: %s
                - Location: %s
                - Date: %s

                Log in to the Lost & Found Portal and open My Posts > Find Matches \
                on your lost item to view it and submit a claim.

                - Lost & Found Portal
                """.formatted(
                lostItem.getUser().getName(),
                score,
                lostItem.getItemName(),
                foundItem.getItemName(),
                foundItem.getDescription(),
                foundItem.getLocation(),
                foundItem.getDate());

        send(lostItem.getUser().getEmail(), subject, body);
    }

    private void send(String to, String subject, String body) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

        } catch (Exception ex) {
            log.warn("Failed to send notification email to {}", to, ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}