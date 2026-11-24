package ru.openmarry.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import ru.openmarry.velocity.OpenMarryVelocity;
import ru.openmarry.velocity.Proposals.Proposal;
import ru.openmarry.velocity.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class MarriageCommands implements SimpleCommand {

    private final OpenMarryVelocity plugin;

    public MarriageCommands(OpenMarryVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player player)) {
            source.sendMessage(plugin.settings().message("only-players"));
            return;
        }
        String[] args = invocation.arguments();
        switch (invocation.alias().toLowerCase(Locale.ROOT)) {
            case "marry" -> handleMarry(player, args);
            case "divorce" -> handleDivorce(player);
            case "heartcolor" -> handleHeartColor(player, args);
            default -> {
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> suggestions = new ArrayList<>();
        if (args.length > 1) {
            return suggestions;
        }
        String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        switch (invocation.alias().toLowerCase(Locale.ROOT)) {
            case "marry" -> {
                addMatching(suggestions, List.of("accept", "deny"), prefix);
                addMatching(suggestions, plugin.server().getAllPlayers().stream().map(Player::getUsername).toList(), prefix);
            }
            case "heartcolor" -> addMatching(suggestions, plugin.settings().colors().keySet(), prefix);
            default -> {
            }
        }
        return suggestions;
    }

    private void addMatching(List<String> target, Iterable<String> options, String prefix) {
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                target.add(option);
            }
        }
    }

    private void handleMarry(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(plugin.settings().message("usage-marry"));
            return;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "accept" -> acceptProposal(player);
            case "deny" -> denyProposal(player);
            default -> sendProposal(player, args[0]);
        }
    }

    private void sendProposal(Player player, String targetName) {
        Settings settings = plugin.settings();
        Optional<Player> found = plugin.server().getPlayer(targetName);
        if (found.isEmpty()) {
            player.sendMessage(settings.message("player-offline", "{player}", targetName));
            return;
        }
        Player target = found.get();
        if (target.equals(player)) {
            player.sendMessage(settings.message("self-marry"));
            return;
        }
        if (plugin.store().isMarried(player.getUniqueId())) {
            player.sendMessage(settings.message("already-married-self"));
            return;
        }
        if (plugin.store().isMarried(target.getUniqueId())) {
            player.sendMessage(settings.message("already-married-other", "{player}", target.getUsername()));
            return;
        }

        int seconds = settings.proposalSeconds();
        long expiresAt = System.currentTimeMillis() + seconds * 1000L;
        plugin.proposals().offer(target.getUniqueId(), new Proposal(player.getUniqueId(), player.getUsername(), expiresAt));

        player.sendMessage(settings.message("proposal-sent", "{player}", target.getUsername()));
        target.sendMessage(settings.message("proposal-received",
                "{player}", player.getUsername(), "{seconds}", String.valueOf(seconds)));
    }

    private void acceptProposal(Player player) {
        Settings settings = plugin.settings();
        Proposal proposal = plugin.proposals().take(player.getUniqueId());
        if (proposal == null || proposal.expired()) {
            player.sendMessage(settings.message("no-proposal"));
            return;
        }
        Optional<Player> found = plugin.server().getPlayer(proposal.from());
        if (found.isEmpty()) {
            player.sendMessage(settings.message("proposer-offline"));
            return;
        }
        Player proposer = found.get();
        if (plugin.store().isMarried(player.getUniqueId())) {
            player.sendMessage(settings.message("already-married-self"));
            return;
        }
        if (plugin.store().isMarried(proposer.getUniqueId())) {
            player.sendMessage(settings.message("already-married-other", "{player}", proposer.getUsername()));
            return;
        }

        plugin.store().marry(proposer.getUniqueId(), player.getUniqueId(), settings.defaultColor());
        player.sendMessage(settings.message("married", "{player}", proposer.getUsername()));
        proposer.sendMessage(settings.message("married", "{player}", player.getUsername()));

        plugin.syncCouple(player.getUniqueId(), proposer.getUniqueId());
        plugin.playWedding(player, proposer.getUniqueId());
        plugin.playWedding(proposer, player.getUniqueId());
    }

    private void denyProposal(Player player) {
        Settings settings = plugin.settings();
        Proposal proposal = plugin.proposals().take(player.getUniqueId());
        if (proposal == null) {
            player.sendMessage(settings.message("no-proposal"));
            return;
        }
        player.sendMessage(settings.message("proposal-denied-self", "{player}", proposal.fromName()));
        plugin.server().getPlayer(proposal.from()).ifPresent(proposer ->
                proposer.sendMessage(settings.message("proposal-denied-other", "{player}", player.getUsername())));
    }

    private void handleDivorce(Player player) {
        Settings settings = plugin.settings();
        UUID partnerId = plugin.store().divorce(player.getUniqueId());
        if (partnerId == null) {
            player.sendMessage(settings.message("not-married"));
            return;
        }
        player.sendMessage(settings.message("divorced-self"));
        plugin.server().getPlayer(partnerId).ifPresent(partner ->
                partner.sendMessage(settings.message("divorced-other", "{player}", player.getUsername())));
        plugin.syncCouple(player.getUniqueId(), partnerId);
    }

    private void handleHeartColor(Player player, String[] args) {
        Settings settings = plugin.settings();
        Map<String, String> colors = settings.colors();
        String available = String.join(", ", colors.keySet());
        if (args.length == 0) {
            player.sendMessage(settings.message("usage-heartcolor", "{colors}", available));
            return;
        }
        UUID partnerId = plugin.store().partnerOf(player.getUniqueId());
        if (partnerId == null) {
            player.sendMessage(settings.message("not-married"));
            return;
        }
        String name = args[0].toLowerCase(Locale.ROOT);
        if (!colors.containsKey(name)) {
            player.sendMessage(settings.message("unknown-color", "{colors}", available));
            return;
        }

        plugin.store().changeColor(player.getUniqueId(), name);
        String code = colors.get(name);
        player.sendMessage(settings.message("color-set", "{color}", code, "{heart}", settings.symbol()));
        plugin.server().getPlayer(partnerId).ifPresent(partner ->
                partner.sendMessage(settings.message("color-changed-other",
                        "{player}", player.getUsername(), "{color}", code, "{heart}", settings.symbol())));
        plugin.syncCouple(player.getUniqueId(), partnerId);
    }
}
