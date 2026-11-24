package ru.openmarry.paper.marriage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProposalBook {

    public record Proposal(UUID from, String fromName, long expiresAt) {

        public boolean expired() {
            return expiresAt < System.currentTimeMillis();
        }
    }

    private final Map<UUID, Proposal> pending = new ConcurrentHashMap<>();

    public void offer(UUID target, Proposal proposal) {
        pending.put(target, proposal);
    }

    public Proposal take(UUID target) {
        return pending.remove(target);
    }
}
