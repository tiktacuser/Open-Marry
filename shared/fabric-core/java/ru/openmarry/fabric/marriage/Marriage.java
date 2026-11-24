package ru.openmarry.fabric.marriage;

import java.util.UUID;

public record Marriage(UUID first, UUID second, String color) {

    public UUID partnerOf(UUID player) {
        return player.equals(first) ? second : first;
    }

    public Marriage withColor(String newColor) {
        return new Marriage(first, second, newColor);
    }
}
