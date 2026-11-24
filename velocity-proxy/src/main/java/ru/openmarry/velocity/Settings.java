package ru.openmarry.velocity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public final class Settings {

    private static final String COLOR_PREFIX = "heart.color.";
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private final Properties properties = new Properties();

    public Settings(Path dataDirectory) throws IOException {
        Files.createDirectories(dataDirectory);
        Path file = dataDirectory.resolve("config.properties");
        if (Files.notExists(file)) {
            copyDefaults(file);
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
    }

    public String symbol() {
        return properties.getProperty("heart.symbol", "❤");
    }

    public String defaultColor() {
        return properties.getProperty("heart.default-color", "red");
    }

    public int proposalSeconds() {
        try {
            return Integer.parseInt(properties.getProperty("proposal.expire-seconds", "60").trim());
        } catch (NumberFormatException e) {
            return 60;
        }
    }

    public Map<String, String> colors() {
        Map<String, String> colors = new TreeMap<>();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(COLOR_PREFIX)) {
                colors.put(key.substring(COLOR_PREFIX.length()), properties.getProperty(key));
            }
        }
        return colors;
    }

    public String colorCode(String name) {
        return properties.getProperty(COLOR_PREFIX + name, "&c");
    }

    public Component message(String key, String... replacements) {
        String text = properties.getProperty("msg." + key, key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace(replacements[i], replacements[i + 1]);
        }
        return SERIALIZER.deserialize(properties.getProperty("msg.prefix", "") + text);
    }

    private void copyDefaults(Path target) throws IOException {
        try (InputStream input = Settings.class.getResourceAsStream("/config.properties")) {
            if (input == null) {
                throw new IOException("config.properties is missing from the jar");
            }
            Files.copy(input, target);
        }
    }
}
