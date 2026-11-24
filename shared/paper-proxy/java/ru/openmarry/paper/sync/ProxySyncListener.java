package ru.openmarry.paper.sync;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import ru.openmarry.paper.effects.WeddingEffects;
import ru.openmarry.paper.marriage.ProxyMarriageCache;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

public final class ProxySyncListener implements PluginMessageListener {

    public static final String CHANNEL = "openmarry:sync";

    private static final byte STATE = 1;
    private static final byte WEDDING = 2;

    private final Logger logger;
    private final ProxyMarriageCache cache;
    private final WeddingEffects effects;

    public ProxySyncListener(Logger logger, ProxyMarriageCache cache, WeddingEffects effects) {
        this.logger = logger;
        this.cache = cache;
        this.effects = effects;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player carrier, byte[] message) {
        if (!CHANNEL.equals(channel)) {
            return;
        }
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(message))) {
            byte type = input.readByte();
            UUID player = readUuid(input);
            if (type == STATE) {
                readState(input, player);
            } else if (type == WEDDING) {
                playWedding(player);
            }
        } catch (IOException e) {
            logger.warning("Could not read marriage data from the proxy: " + e.getMessage());
        }
    }

    private void readState(DataInputStream input, UUID player) throws IOException {
        if (!input.readBoolean()) {
            cache.clear(player);
            return;
        }
        UUID partner = readUuid(input);
        String heartColorCode = input.readUTF();
        cache.update(player, partner, heartColorCode);
    }

    private void playWedding(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            effects.play(player);
        }
    }

    private UUID readUuid(DataInputStream input) throws IOException {
        return new UUID(input.readLong(), input.readLong());
    }
}
