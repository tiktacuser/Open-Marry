package ru.openmarry.fabric.marriage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ProposalBook {

    public record Proposal(UUID from, String fromName, long expiresAt) {

        public boolean expired() {
            return expiresAt < System.currentTimeMillis();
        }
    }

    private final Map<UUID, Proposal> pending = new HashMap<>();

    public void offer(UUID target, Proposal proposal) {
        pending.put(target, proposal);
    }

    public Proposal take(UUID target) {
        return pending.remove(target);
    }
}
