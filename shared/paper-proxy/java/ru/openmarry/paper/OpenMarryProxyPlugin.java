package ru.openmarry.paper;

import org.bukkit.plugin.java.JavaPlugin;
import ru.openmarry.paper.effects.WeddingEffects;
import ru.openmarry.paper.listener.CacheCleanupListener;
import ru.openmarry.paper.listener.PartnerProtectionListener;
import ru.openmarry.paper.marriage.ProxyMarriageCache;
import ru.openmarry.paper.placeholder.HeartExpansion;
import ru.openmarry.paper.sync.ProxySyncListener;
import ru.openmarry.paper.task.PartnerRegeneration;

public final class OpenMarryProxyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ProxyMarriageCache cache = new ProxyMarriageCache();
        ProxySyncListener syncListener = new ProxySyncListener(getLogger(), cache, new WeddingEffects(this));
        getServer().getMessenger().registerIncomingPluginChannel(this, ProxySyncListener.CHANNEL, syncListener);

        getServer().getPluginManager().registerEvents(new CacheCleanupListener(cache), this);
        getServer().getPluginManager().registerEvents(new PartnerProtectionListener(this, cache), this);

        PartnerRegeneration.start(this, cache);
        HeartExpansion.registerIfAvailable(this, cache);
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }
}
