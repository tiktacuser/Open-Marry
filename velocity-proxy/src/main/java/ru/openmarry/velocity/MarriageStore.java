package ru.openmarry.velocity;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MarriageStore {

    private static final String PARTNER_PREFIX = "partner.";
    private static final String COLOR_PREFIX = "color.";

    private final Path file;
    private final Logger logger;
    private final Map<UUID, UUID> partners = new ConcurrentHashMap<>();
    private final Map<UUID, String> colors = new ConcurrentHashMap<>();

    public MarriageStore(Path file, Logger logger) {
        this.file = file;
        this.logger = logger;
        load();
    }

    public boolean isMarried(UUID player) {
        return partners.containsKey(player);
    }

    public UUID partnerOf(UUID player) {
        return partners.get(player);
    }

    public String colorOf(UUID player) {
        return colors.get(player);
    }

    public synchronized void marry(UUID first, UUID second, String color) {
        partners.put(first, second);
        partners.put(second, first);
        colors.put(first, color);
        colors.put(second, color);
        save();
    }

    public synchronized UUID divorce(UUID player) {
        UUID partner = partners.remove(player);
        if (partner == null) {
            return null;
        }
        partners.remove(partner);
        colors.remove(player);
        colors.remove(partner);
        save();
        return partner;
    }

    public synchronized void changeColor(UUID player, String color) {
        UUID partner = partners.get(player);
        if (partner == null) {
            return;
        }
        colors.put(player, color);
        colors.put(partner, color);
        save();
    }

    private void load() {
        if (Files.notExists(file)) {
            return;
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            logger.error("Could not read data.properties", e);
            return;
        }
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (key.startsWith(PARTNER_PREFIX)) {
                partners.put(UUID.fromString(key.substring(PARTNER_PREFIX.length())), UUID.fromString(value));
            } else if (key.startsWith(COLOR_PREFIX)) {
                colors.put(UUID.fromString(key.substring(COLOR_PREFIX.length())), value);
            }
        }
    }

    private void save() {
        Properties properties = new Properties();
        partners.forEach((player, partner) -> properties.setProperty(PARTNER_PREFIX + player, partner.toString()));
        colors.forEach((player, color) -> properties.setProperty(COLOR_PREFIX + player, color));
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                properties.store(writer, null);
            }
        } catch (IOException e) {
            logger.error("Could not save data.properties", e);
        }
    }
}
