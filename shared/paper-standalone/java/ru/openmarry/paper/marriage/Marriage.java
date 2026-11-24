package ru.openmarry.paper.marriage;

import java.util.UUID;

public final class Marriage {

    private final UUID first;
    private final UUID second;
    private String color;

    public Marriage(UUID first, UUID second, String color) {
        this.first = first;
        this.second = second;
        this.color = color;
    }

    public UUID first() {
        return first;
    }

    public UUID second() {
        return second;
    }

    public String color() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public UUID partnerOf(UUID player) {
        return player.equals(first) ? second : first;
    }
}
