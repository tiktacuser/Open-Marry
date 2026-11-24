package ru.openmarry.paper.marriage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class HeartPalette {

    private final JavaPlugin plugin;

    public HeartPalette(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String symbol() {
        return plugin.getConfig().getString("heart.symbol", "❤");
    }

    public String defaultName() {
        return plugin.getConfig().getString("heart.default-color", "red");
    }

    public Set<String> names() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("heart.colors");
        return section == null ? Set.of() : section.getKeys(false);
    }

    public boolean exists(String name) {
        return names().contains(name);
    }

    public String codeOf(String name) {
        return plugin.getConfig().getString("heart.colors." + name, "&c");
    }

    public String joinedNames() {
        return String.join(", ", names());
    }
}
