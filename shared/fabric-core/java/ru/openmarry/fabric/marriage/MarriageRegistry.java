package ru.openmarry.fabric.marriage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MarriageRegistry {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final class Data {
        private List<Marriage> marriages = new ArrayList<>();
    }

    private final Path file;
    private final Map<UUID, Marriage> byPlayer = new HashMap<>();

    private MarriageRegistry(Path file) {
        this.file = file;
    }

    public static MarriageRegistry load(Path file) {
        MarriageRegistry registry = new MarriageRegistry(file);
        registry.read();
        return registry;
    }

    public boolean isMarried(UUID player) {
        return byPlayer.containsKey(player);
    }

    public Marriage marriageOf(UUID player) {
        return byPlayer.get(player);
    }

    public void marry(UUID first, UUID second, String color) {
        register(new Marriage(first, second, color));
        save();
    }

    public Marriage divorce(UUID player) {
        Marriage marriage = byPlayer.remove(player);
        if (marriage == null) {
            return null;
        }
        byPlayer.remove(marriage.partnerOf(player));
        save();
        return marriage;
    }

    public void changeColor(UUID player, String color) {
        Marriage marriage = byPlayer.get(player);
        if (marriage == null) {
            return;
        }
        register(marriage.withColor(color));
        save();
    }

    private void register(Marriage marriage) {
        byPlayer.put(marriage.first(), marriage);
        byPlayer.put(marriage.second(), marriage);
    }

    private void read() {
        if (Files.notExists(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data != null && data.marriages != null) {
                data.marriages.forEach(this::register);
            }
        } catch (IOException | JsonParseException e) {
            throw new IllegalStateException("Could not read marriage data: " + file, e);
        }
    }

    private void save() {
        Data data = new Data();
        data.marriages = new ArrayList<>(new LinkedHashSet<>(byPlayer.values()));
        try {
            Files.writeString(file, GSON.toJson(data), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save marriage data: " + file, e);
        }
    }
}
