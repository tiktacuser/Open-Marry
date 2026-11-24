package ru.openmarry.paper.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.openmarry.paper.effects.WeddingEffects;
import ru.openmarry.paper.marriage.HeartPalette;
import ru.openmarry.paper.marriage.Marriage;
import ru.openmarry.paper.marriage.MarriageRegistry;
import ru.openmarry.paper.marriage.ProposalBook;
import ru.openmarry.paper.marriage.ProposalBook.Proposal;
import ru.openmarry.paper.message.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class MarriageCommands implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final MarriageRegistry marriages;
    private final ProposalBook proposals;
    private final HeartPalette palette;
    private final Messages messages;
    private final WeddingEffects effects;

    public MarriageCommands(JavaPlugin plugin, MarriageRegistry marriages, ProposalBook proposals,
                            HeartPalette palette, Messages messages, WeddingEffects effects) {
        this.plugin = plugin;
        this.marriages = marriages;
        this.proposals = proposals;
        this.palette = palette;
        this.messages = messages;
        this.effects = effects;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "only-players");
            return true;
        }
        switch (command.getName().toLowerCase(Locale.ROOT)) {
            case "marry" -> handleMarry(player, args);
            case "divorce" -> handleDivorce(player);
            case "heartcolor" -> handleHeartColor(player, args);
            default -> {
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length != 1) {
            return suggestions;
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        switch (command.getName().toLowerCase(Locale.ROOT)) {
            case "marry" -> {
                addMatching(suggestions, List.of("accept", "deny"), prefix);
                addMatching(suggestions, Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), prefix);
            }
            case "heartcolor" -> addMatching(suggestions, palette.names(), prefix);
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
            messages.send(player, "usage-marry");
            return;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "accept" -> acceptProposal(player);
            case "deny" -> denyProposal(player);
            default -> sendProposal(player, args[0]);
        }
    }

    private void sendProposal(Player player, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            messages.send(player, "player-offline", "{player}", targetName);
            return;
        }
        if (target.equals(player)) {
            messages.send(player, "self-marry");
            return;
        }
        if (marriages.isMarried(player.getUniqueId())) {
            messages.send(player, "already-married-self");
            return;
        }
        if (marriages.isMarried(target.getUniqueId())) {
            messages.send(player, "already-married-other", "{player}", target.getName());
            return;
        }

        int seconds = plugin.getConfig().getInt("proposal.expire-seconds", 60);
        long expiresAt = System.currentTimeMillis() + seconds * 1000L;
        proposals.offer(target.getUniqueId(), new Proposal(player.getUniqueId(), player.getName(), expiresAt));

        messages.send(player, "proposal-sent", "{player}", target.getName());
        messages.send(target, "proposal-received", "{player}", player.getName(), "{seconds}", String.valueOf(seconds));
    }

    private void acceptProposal(Player player) {
        Proposal proposal = proposals.take(player.getUniqueId());
        if (proposal == null || proposal.expired()) {
            messages.send(player, "no-proposal");
            return;
        }
        Player proposer = Bukkit.getPlayer(proposal.from());
        if (proposer == null) {
            messages.send(player, "proposer-offline");
            return;
        }
        if (marriages.isMarried(player.getUniqueId())) {
            messages.send(player, "already-married-self");
            return;
        }
        if (marriages.isMarried(proposer.getUniqueId())) {
            messages.send(player, "already-married-other", "{player}", proposer.getName());
            return;
        }

        marriages.marry(proposer.getUniqueId(), player.getUniqueId(), palette.defaultName());
        messages.send(player, "married", "{player}", proposer.getName());
        messages.send(proposer, "married", "{player}", player.getName());
        effects.play(player);
        effects.play(proposer);
    }

    private void denyProposal(Player player) {
        Proposal proposal = proposals.take(player.getUniqueId());
        if (proposal == null) {
            messages.send(player, "no-proposal");
            return;
        }
        messages.send(player, "proposal-denied-self", "{player}", proposal.fromName());
        Player proposer = Bukkit.getPlayer(proposal.from());
        if (proposer != null) {
            messages.send(proposer, "proposal-denied-other", "{player}", player.getName());
        }
    }

    private void handleDivorce(Player player) {
        Marriage marriage = marriages.divorce(player.getUniqueId());
        if (marriage == null) {
            messages.send(player, "not-married");
            return;
        }
        messages.send(player, "divorced-self");
        Player partner = Bukkit.getPlayer(marriage.partnerOf(player.getUniqueId()));
        if (partner != null) {
            messages.send(partner, "divorced-other", "{player}", player.getName());
        }
    }

    private void handleHeartColor(Player player, String[] args) {
        if (args.length == 0) {
            messages.send(player, "usage-heartcolor", "{colors}", palette.joinedNames());
            return;
        }
        Marriage marriage = marriages.marriageOf(player.getUniqueId());
        if (marriage == null) {
            messages.send(player, "not-married");
            return;
        }
        String name = args[0].toLowerCase(Locale.ROOT);
        if (!palette.exists(name)) {
            messages.send(player, "unknown-color", "{colors}", palette.joinedNames());
            return;
        }

        marriages.changeColor(player.getUniqueId(), name);
        String code = palette.codeOf(name);
        messages.send(player, "color-set", "{color}", code, "{heart}", palette.symbol());

        UUID partnerId = marriage.partnerOf(player.getUniqueId());
        Player partner = Bukkit.getPlayer(partnerId);
        if (partner != null) {
            messages.send(partner, "color-changed-other",
                    "{player}", player.getName(), "{color}", code, "{heart}", palette.symbol());
        }
    }
}
