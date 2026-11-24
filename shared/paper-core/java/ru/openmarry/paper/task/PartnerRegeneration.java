package ru.openmarry.paper.task;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.openmarry.paper.marriage.MarriageView;

import java.util.UUID;

public final class PartnerRegeneration {

    private PartnerRegeneration() {
    }

    public static void start(JavaPlugin plugin, MarriageView marriages) {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("regeneration.enabled", false)) {
            return;
        }
        long interval = Math.max(1, config.getInt("regeneration.interval-ticks", 40));
        Bukkit.getScheduler().runTaskTimer(plugin, () -> apply(config, marriages), interval, interval);
    }

    private static void apply(FileConfiguration config, MarriageView marriages) {
        double radius = config.getDouble("regeneration.radius", 10.0);
        double radiusSquared = radius * radius;
        int amplifier = Math.max(0, config.getInt("regeneration.level", 1) - 1);
        int duration = config.getInt("regeneration.duration-ticks", 60);

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID partnerId = marriages.partnerOf(player.getUniqueId());
            if (partnerId == null) {
                continue;
            }
            Player partner = Bukkit.getPlayer(partnerId);
            if (partner == null || !partner.getWorld().equals(player.getWorld())) {
                continue;
            }
            if (player.getLocation().distanceSquared(partner.getLocation()) > radiusSquared) {
                continue;
            }
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION, duration, amplifier, false, false, true));
        }
    }
}
