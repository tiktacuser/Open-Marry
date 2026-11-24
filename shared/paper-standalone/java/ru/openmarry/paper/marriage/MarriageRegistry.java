package ru.openmarry.paper.marriage;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MarriageRegistry implements MarriageView {

    private final JavaPlugin plugin;
    private final HeartPalette palette;
    private final File file;
    private final Map<UUID, Marriage> byPlayer = new HashMap<>();

    public MarriageRegistry(JavaPlugin plugin, HeartPalette palette) {
        this.plugin = plugin;
        this.palette = palette;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        load();
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
        marriage.setColor(color);
        save();
    }

    @Override
    public UUID partnerOf(UUID player) {
        Marriage marriage = byPlayer.get(player);
        return marriage == null ? null : marriage.partnerOf(player);
    }

    @Override
    public String heartColorCode(UUID player) {
        Marriage marriage = byPlayer.get(player);
        return marriage == null ? null : palette.codeOf(marriage.color());
    }

    private void register(Marriage marriage) {
        byPlayer.put(marriage.first(), marriage);
        byPlayer.put(marriage.second(), marriage);
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> row : yaml.getMapList("marriages")) {
            UUID first = UUID.fromString(String.valueOf(row.get("first")));
            UUID second = UUID.fromString(String.valueOf(row.get("second")));
            String color = String.valueOf(row.get("color"));
            register(new Marriage(first, second, color));
        }
    }

    private void save() {
        List<Map<String, String>> rows = new ArrayList<>();
        for (Marriage marriage : new LinkedHashSet<>(byPlayer.values())) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("first", marriage.first().toString());
            row.put("second", marriage.second().toString());
            row.put("color", marriage.color());
            rows.add(row);
        }

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("marriages", rows);
        try {
            plugin.getDataFolder().mkdirs();
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }
}
