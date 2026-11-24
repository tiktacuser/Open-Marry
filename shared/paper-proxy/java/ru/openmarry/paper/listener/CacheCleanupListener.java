package ru.openmarry.paper.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.openmarry.paper.marriage.ProxyMarriageCache;

public final class CacheCleanupListener implements Listener {

    private final ProxyMarriageCache cache;

    public CacheCleanupListener(ProxyMarriageCache cache) {
        this.cache = cache;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cache.clear(event.getPlayer().getUniqueId());
    }
}
