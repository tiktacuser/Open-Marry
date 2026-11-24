package ru.openmarry.paper.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import ru.openmarry.paper.marriage.MarriageView;

public final class HeartExpansion extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private final MarriageView marriages;

    public HeartExpansion(JavaPlugin plugin, MarriageView marriages) {
        this.plugin = plugin;
        this.marriages = marriages;
    }

    public static void registerIfAvailable(JavaPlugin plugin, MarriageView marriages) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            plugin.getLogger().warning("PlaceholderAPI not found, the %hug_heart% placeholder will not work.");
            return;
        }
        new HeartExpansion(plugin, marriages).register();
        plugin.getLogger().info("Registered the %hug_heart% placeholder.");
    }

    @Override
    public String getIdentifier() {
        return "hug";
    }

    @Override
    public String getAuthor() {
        return "OpenMarry";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || !params.equalsIgnoreCase("heart")) {
            return null;
        }
        String colorCode = marriages.heartColorCode(player.getUniqueId());
        if (colorCode == null) {
            return plugin.getConfig().getString("heart.empty-output", "");
        }
        return colorCode + plugin.getConfig().getString("heart.symbol", "❤");
    }
}
