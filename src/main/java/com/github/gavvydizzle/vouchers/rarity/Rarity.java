package com.github.gavvydizzle.vouchers.rarity;

public record Rarity(String id, String colorCode, String name) {
    @Override
    public String toString() {
        return id;
    }
}