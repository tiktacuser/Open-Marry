package ru.openmarry.paper.effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class WeddingEffects {

    private final JavaPlugin plugin;

    public WeddingEffects(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void play(Player player) {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("wedding-effects.enabled", true)) {
            return;
        }

        int interval = Math.max(1, config.getInt("wedding-effects.interval-ticks", 10));
        long duration = config.getInt("wedding-effects.duration-seconds", 10) * 20L;
        double height = config.getDouble("wedding-effects.height-offset", 2.3);
        int count = config.getInt("wedding-effects.particle-count", 3);

        new BukkitRunnable() {
            private long elapsed;

            @Override
            public void run() {
                if (elapsed >= duration || !player.isOnline()) {
                    cancel();
                    return;
                }
                elapsed += interval;
                Location location = player.getLocation().add(0, height, 0);
                player.getWorld().spawnParticle(Particle.HEART, location, count, 0.3, 0.2, 0.3, 0.0);
            }
        }.runTaskTimer(plugin, 0L, interval);
    }
}
