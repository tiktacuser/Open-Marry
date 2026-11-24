package ru.openmarry.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;
import ru.openmarry.velocity.command.MarriageCommands;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "openmarry",
        name = "OpenMarry",
        version = "1.0.0",
        description = "Marriages and heart icons across a whole server network",
        authors = {"OpenMarry"}
)
public final class OpenMarryVelocity {

    public static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.create("openmarry", "sync");

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Proposals proposals = new Proposals();

    private Settings settings;
    private MarriageStore store;

    @Inject
    public OpenMarryVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        try {
            settings = new Settings(dataDirectory);
        } catch (IOException e) {
            logger.error("Could not load config.properties", e);
            return;
        }
        store = new MarriageStore(dataDirectory.resolve("data.properties"), logger);

        server.getChannelRegistrar().register(CHANNEL);

        CommandManager commandManager = server.getCommandManager();
        MarriageCommands commands = new MarriageCommands(this);
        for (String name : new String[]{"marry", "divorce", "heartcolor"}) {
            commandManager.register(commandManager.metaBuilder(name).build(), commands);
        }
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        server.getScheduler()
                .buildTask(this, () -> syncPlayer(player))
                .delay(1, TimeUnit.SECONDS)
                .schedule();
    }

    public ProxyServer server() {
        return server;
    }

    public Settings settings() {
        return settings;
    }

    public MarriageStore store() {
        return store;
    }

    public Proposals proposals() {
        return proposals;
    }

    public void syncPlayer(Player player) {
        UUID id = player.getUniqueId();
        UUID partner = store.partnerOf(id);
        byte[] data = SyncProtocol.state(id, partner, partner == null ? null : heartColorCode(id));
        send(player, data);
    }

    public void syncCouple(UUID first, UUID second) {
        server.getPlayer(first).ifPresent(this::syncPlayer);
        server.getPlayer(second).ifPresent(this::syncPlayer);
    }

    public void playWedding(Player player, UUID partner) {
        send(player, SyncProtocol.wedding(player.getUniqueId(), partner));
    }

    private String heartColorCode(UUID player) {
        String color = store.colorOf(player);
        return settings.colorCode(color != null ? color : settings.defaultColor());
    }

    private void send(Player player, byte[] data) {
        player.getCurrentServer().ifPresent(connection -> connection.sendPluginMessage(CHANNEL, data));
    }
}
