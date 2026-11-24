package ru.openmarry.fabric.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MarriageSettings {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public Heart heart = new Heart();
    public ProposalRules proposal = new ProposalRules();
    public Wedding wedding = new Wedding();
    public Protection protection = new Protection();
    public Regeneration regeneration = new Regeneration();
    public Map<String, String> messages = defaultMessages();

    public static MarriageSettings load(Path file) {
        try {
            if (Files.notExists(file)) {
                MarriageSettings defaults = new MarriageSettings();
                Files.writeString(file, GSON.toJson(defaults), StandardCharsets.UTF_8);
                return defaults;
            }
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                MarriageSettings loaded = GSON.fromJson(reader, MarriageSettings.class);
                return loaded != null ? loaded : new MarriageSettings();
            }
        } catch (IOException | JsonParseException e) {
            throw new IllegalStateException("Could not read OpenMarry settings: " + file, e);
        }
    }

    public String message(String key) {
        return messages.getOrDefault(key, key);
    }

    public static final class Heart {
        public String symbol = "❤";
        public String emptyOutput = "";
        public String defaultColor = "red";
        public Map<String, String> colors = defaultColors();

        public boolean hasColor(String name) {
            return colors.containsKey(name);
        }

        public String colorCode(String name) {
            return colors.getOrDefault(name, "&c");
        }
    }

    public static final class ProposalRules {
        public int expireSeconds = 60;
    }

    public static final class Wedding {
        public boolean enabled = true;
        public int durationSeconds = 10;
        public int intervalTicks = 10;
        public double heightOffset = 2.3;
        public int particleCount = 3;
    }

    public static final class Protection {
        public boolean blockPartnerDamage = true;
    }

    public static final class Regeneration {
        public boolean enabled = false;
        public double radius = 10.0;
        public int level = 1;
        public int intervalTicks = 40;
        public int durationTicks = 60;
    }

    private static Map<String, String> defaultColors() {
        Map<String, String> colors = new LinkedHashMap<>();
        colors.put("red", "&c");
        colors.put("pink", "&d");
        colors.put("gold", "&6");
        colors.put("yellow", "&e");
        colors.put("green", "&a");
        colors.put("aqua", "&b");
        colors.put("blue", "&9");
        colors.put("purple", "&5");
        colors.put("white", "&f");
        colors.put("gray", "&7");
        return colors;
    }

    private static Map<String, String> defaultMessages() {
        Map<String, String> messages = new LinkedHashMap<>();
        messages.put("prefix", "&d❤ &7");
        messages.put("usage-marry", "&cUsage: &f/marry <player> &7| &f/marry accept &7| &f/marry deny");
        messages.put("usage-heartcolor", "&cUsage: &f/heartcolor <color>&7. Available: &f{colors}");
        messages.put("self-marry", "&cYou can't marry yourself.");
        messages.put("already-married-self", "&cYou are already married.");
        messages.put("already-married-other", "&cPlayer &f{player} &cis already married.");
        messages.put("proposal-sent", "&7You proposed to &f{player}&7. Waiting for an answer.");
        messages.put("proposal-received", "&f{player} &7has proposed to you! Type &f/marry accept &7or &f/marry deny &7within {seconds} seconds.");
        messages.put("no-proposal", "&cYou have no pending proposal, or it has expired.");
        messages.put("proposer-offline", "&cThe player who proposed to you has left the game.");
        messages.put("proposal-denied-self", "&7You declined the proposal from &f{player}&7.");
        messages.put("proposal-denied-other", "&f{player} &cdeclined your proposal.");
        messages.put("married", "&d❤ Congratulations! You are now married to &f{player}&d!");
        messages.put("not-married", "&cYou are not married.");
        messages.put("divorced-self", "&7You are now divorced.");
        messages.put("divorced-other", "&f{player} &7divorced you.");
        messages.put("unknown-color", "&cUnknown color. Available: &f{colors}");
        messages.put("color-set", "&7Your marriage heart color: {color}&l{heart}");
        messages.put("color-changed-other", "&f{player} &7changed your marriage heart color to {color}&l{heart}");
        return messages;
    }
}
