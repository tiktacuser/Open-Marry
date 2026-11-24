package ru.openmarry.paper.marriage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProxyMarriageCache implements MarriageView {

    private record Entry(UUID partner, String heartColorCode) {
    }

    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();

    public void update(UUID player, UUID partner, String heartColorCode) {
        entries.put(player, new Entry(partner, heartColorCode));
    }

    public void clear(UUID player) {
        entries.remove(player);
    }

    @Override
    public UUID partnerOf(UUID player) {
        Entry entry = entries.get(player);
        return entry == null ? null : entry.partner();
    }

    @Override
    public String heartColorCode(UUID player) {
        Entry entry = entries.get(player);
        return entry == null ? null : entry.heartColorCode();
    }
}
