package ru.openmarry.fabric.effects;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import ru.openmarry.fabric.config.MarriageSettings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class WeddingEffects {

    private static final int TICKS_PER_SECOND = 20;

    private final MarriageSettings settings;
    private final Map<UUID, Integer> remainingTicks = new HashMap<>();

    public WeddingEffects(MarriageSettings settings) {
        this.settings = settings;
    }

    public void start(UUID player) {
        if (settings.wedding.enabled) {
            remainingTicks.put(player, settings.wedding.durationSeconds * TICKS_PER_SECOND);
        }
    }

    public void tick(MinecraftServer server) {
        if (remainingTicks.isEmpty()) {
            return;
        }
        int interval = Math.max(1, settings.wedding.intervalTicks);

        Iterator<Map.Entry<UUID, Integer>> iterator = remainingTicks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            int remaining = entry.getValue() - 1;
            if (player == null || remaining <= 0) {
                iterator.remove();
                continue;
            }
            entry.setValue(remaining);
            if (remaining % interval == 0) {
                spawnHearts(player);
            }
        }
    }

    private void spawnHearts(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles(ParticleTypes.HEART,
                player.getX(), player.getY() + settings.wedding.heightOffset, player.getZ(),
                settings.wedding.particleCount, 0.3, 0.2, 0.3, 0.0);
    }
}
