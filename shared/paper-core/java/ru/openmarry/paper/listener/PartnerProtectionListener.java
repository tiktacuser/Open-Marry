package ru.openmarry.paper.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.openmarry.paper.marriage.MarriageView;

import java.util.UUID;

public final class PartnerProtectionListener implements Listener {

    private final JavaPlugin plugin;
    private final MarriageView marriages;

    public PartnerProtectionListener(JavaPlugin plugin, MarriageView marriages) {
        this.plugin = plugin;
        this.marriages = marriages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("protection.block-partner-damage", true)) {
            return;
        }
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player attacker = attackerOf(event.getDamager());
        if (attacker == null) {
            return;
        }
        UUID partner = marriages.partnerOf(victim.getUniqueId());
        if (attacker.getUniqueId().equals(partner)) {
            event.setCancelled(true);
        }
    }

    private Player attackerOf(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            return shooter;
        }
        return null;
    }
}
