package com.lostfound.lostfoundportal.service;

import com.lostfound.lostfoundportal.dto.ItemMatch;
import com.lostfound.lostfoundportal.model.Item;
import com.lostfound.lostfoundportal.model.VerificationStatus;
import com.lostfound.lostfoundportal.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Drives a FOUND item through the admin verification pipeline:
 *   SUBMITTED -> PENDING_VERIFICATION -> ADMIN_REVIEW -> APPROVED
 * One call moves an item exactly one step forward - there's no skipping
 * ahead or going back. Only APPROVED found items appear on the public
 * dashboard, in search results, or in Smart Matching results (see
 * ItemRepository.searchItems and ItemMatchingService.findMatchesForLostItem).
 */
@Service
public class ItemVerificationService {

    private final ItemRepository itemRepository;
    private final ItemMatchingService itemMatchingService;
    private final NotificationService notificationService;

    public ItemVerificationService(
            ItemRepository itemRepository,
            ItemMatchingService itemMatchingService,
            NotificationService notificationService) {

        this.itemRepository = itemRepository;
        this.itemMatchingService = itemMatchingService;
        this.notificationService = notificationService;
    }

    /**
     * All found items, unverified ones first (oldest first within each
     * stage) so the admin's review queue naturally floats to the top.
     */
    public List<Item> getFoundItemsForReview() {

        List<Item> foundItems = itemRepository.findByStatus("FOUND");

        foundItems.sort(
                Comparator.<Item, Boolean>comparing(
                                item -> item.getVerificationStatus() == VerificationStatus.APPROVED)
                        .thenComparing(Item::getId));

        return foundItems;
    }

    public Item advanceVerification(Long itemId) {

        Item item = itemRepository.findById(itemId).orElse(null);

        if (item == null || !"FOUND".equals(item.getStatus())) {
            return item;
        }

        VerificationStatus current = item.getVerificationStatus() == null
                ? VerificationStatus.SUBMITTED
                : item.getVerificationStatus();

        VerificationStatus next = switch (current) {
            case SUBMITTED -> VerificationStatus.PENDING_VERIFICATION;
            case PENDING_VERIFICATION -> VerificationStatus.ADMIN_REVIEW;
            case ADMIN_REVIEW, APPROVED -> VerificationStatus.APPROVED;
        };

        item.setVerificationStatus(next);

        Item saved = itemRepository.save(item);

        // Only once it clears ADMIN_REVIEW and becomes visible do we check
        // for matches and email lost-item owners - never for an item that
        // hasn't been verified yet.
        if (next == VerificationStatus.APPROVED && current != VerificationStatus.APPROVED) {
            notifyMatchingLostItemOwners(saved);
        }

        return saved;
    }

    private void notifyMatchingLostItemOwners(Item approvedFoundItem) {

        List<ItemMatch> matches = itemMatchingService.findMatchesForFoundItem(approvedFoundItem);

        matches.stream()
                .filter(match -> match.getScore() >= ItemMatchingService.NOTIFY_THRESHOLD)
                .forEach(match -> notificationService.notifyPossibleMatch(
                        match.getItem(), approvedFoundItem, match.getScore()));
    }
}