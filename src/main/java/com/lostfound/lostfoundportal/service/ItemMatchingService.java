package com.lostfound.lostfoundportal.service;

import com.lostfound.lostfoundportal.dto.ItemMatch;
import com.lostfound.lostfoundportal.model.Item;
import com.lostfound.lostfoundportal.model.VerificationStatus;
import com.lostfound.lostfoundportal.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Rule-based "smart matching" between a LOST item report and existing FOUND
 * item reports. Deliberately kept dependency-free (no external API calls) so
 * it always works instantly, regardless of Gemini quota/availability -
 * unlike the AI Image Description feature, this never needs an API key.
 *
 * Score is a weighted 0-100 blend of:
 *  - item name similarity     (35%)
 *  - description similarity   (30%)
 *  - location similarity      (20%)
 *  - date proximity           (15%)
 */
@Service
public class ItemMatchingService {

    private static final int MIN_SCORE_TO_SHOW = 30;
    private static final int MAX_RESULTS = 5;

    // Higher bar than MIN_SCORE_TO_SHOW - used to decide when a match is
    // strong enough to email someone about, vs. merely worth listing on
    // the Possible Matches page.
    public static final int NOTIFY_THRESHOLD = 60;

    private final ItemRepository itemRepository;

    public ItemMatchingService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<ItemMatch> findMatchesForLostItem(Item lostItem) {

        List<Item> foundItems = itemRepository.findByStatus("FOUND");

        return foundItems.stream()
                // Unverified found items aren't shown or matched against
                // until an admin approves them (see VerificationStatus).
                .filter(found -> found.getVerificationStatus() == VerificationStatus.APPROVED)
                .map(found -> new ItemMatch(found, scoreMatch(lostItem, found)))
                .filter(match -> match.getScore() >= MIN_SCORE_TO_SHOW)
                .sorted(Comparator.comparingInt(ItemMatch::getScore).reversed())
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());
    }

    /**
     * The mirror of findMatchesForLostItem: given a FOUND item (called once
     * it's been APPROVED - see ItemVerificationService), find existing LOST
     * reports it might satisfy. Used to decide who to notify by email.
     */
    public List<ItemMatch> findMatchesForFoundItem(Item foundItem) {

        List<Item> lostItems = itemRepository.findByStatus("LOST");

        return lostItems.stream()
                .map(lost -> new ItemMatch(lost, scoreMatch(lost, foundItem)))
                .filter(match -> match.getScore() >= MIN_SCORE_TO_SHOW)
                .sorted(Comparator.comparingInt(ItemMatch::getScore).reversed())
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());
    }

    private int scoreMatch(Item lost, Item found) {

        int nameScore = tokenOverlapScore(lost.getItemName(), found.getItemName());
        int descriptionScore = tokenOverlapScore(lost.getDescription(), found.getDescription());
        int locationScore = tokenOverlapScore(lost.getLocation(), found.getLocation());
        int dateScore = dateProximityScore(lost.getDate(), found.getDate());

        double weighted =
                (nameScore * 0.35)
                + (descriptionScore * 0.30)
                + (locationScore * 0.20)
                + (dateScore * 0.15);

        return (int) Math.round(weighted);
    }

    /**
     * Jaccard similarity (word overlap) between two short pieces of text,
     * as a 0-100 score. E.g. "black leather wallet" vs "black wallet" share
     * 2 of 3 unique words -> 67.
     */
    private int tokenOverlapScore(String textA, String textB) {

        Set<String> tokensA = tokenize(textA);
        Set<String> tokensB = tokenize(textB);

        if (tokensA.isEmpty() || tokensB.isEmpty()) {
            return 0;
        }

        Set<String> intersection = new HashSet<>(tokensA);
        intersection.retainAll(tokensB);

        Set<String> union = new HashSet<>(tokensA);
        union.addAll(tokensB);

        return (int) Math.round((intersection.size() * 100.0) / union.size());
    }

    private Set<String> tokenize(String text) {

        if (text == null || text.isBlank()) {
            return Collections.emptySet();
        }

        return Arrays.stream(text.toLowerCase().split("[^a-z0-9]+"))
                .filter(word -> word.length() > 1)
                .collect(Collectors.toSet());
    }

    /**
     * A found item is almost always found on or shortly after the day it
     * was lost, so dates close together (in that order) score highest.
     * Dates are often remembered approximately, so this degrades gently
     * rather than zeroing out on a mismatch.
     */
    private int dateProximityScore(LocalDate lostDate, LocalDate foundDate) {

        if (lostDate == null || foundDate == null) {
            return 50; // neutral - don't penalize missing dates
        }

        long daysBetween = ChronoUnit.DAYS.between(lostDate, foundDate);

        if (daysBetween < 0) {
            return 40; // found "before" lost, per the recorded dates - still possible
        }
        if (daysBetween <= 3) {
            return 100;
        }
        if (daysBetween <= 7) {
            return 80;
        }
        if (daysBetween <= 14) {
            return 55;
        }
        if (daysBetween <= 30) {
            return 30;
        }

        return 10;
    }
}