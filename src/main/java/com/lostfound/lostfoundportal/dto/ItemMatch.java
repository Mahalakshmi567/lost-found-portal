package com.lostfound.lostfoundportal.dto;

import com.lostfound.lostfoundportal.model.Item;

/**
 * A candidate FOUND item paired with how well it scored against a LOST item,
 * produced by ItemMatchingService. score is 0-100.
 */
public class ItemMatch {

    private final Item item;
    private final int score;

    public ItemMatch(Item item, int score) {
        this.item = item;
        this.score = score;
    }

    public Item getItem() {
        return item;
    }

    public int getScore() {
        return score;
    }
}