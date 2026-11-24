package ru.openmarry.fabric.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import ru.openmarry.fabric.config.MarriageSettings;
import ru.openmarry.fabric.effects.WeddingEffects;
import ru.openmarry.fabric.marriage.Marriage;
import ru.openmarry.fabric.marriage.MarriageRegistry;
import ru.openmarry.fabric.marriage.ProposalBook;
import ru.openmarry.fabric.marriage.ProposalBook.Proposal;
import ru.openmarry.fabric.text.Messages;

import java.util.Locale;
import java.util.UUID;

public final class MarriageCommands {

    private static final String PLAYER_ARGUMENT = "player";
    private static final String COLOR_ARGUMENT = "color";

    private final MarriageSettings settings;
    private final MarriageRegistry marriages;
    private final ProposalBook proposals;
    private final Messages messages;
    private final WeddingEffects effects;

    public MarriageCommands(MarriageSettings settings, MarriageRegistry marriages, ProposalBook proposals,
                            Messages messages, WeddingEffects effects) {
        this.settings = settings;
        this.marriages = marriages;
        this.proposals = proposals;
        this.messages = messages;
        this.effects = effects;
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("marry")
                .executes(this::marryUsage)
                .then(Commands.literal("accept").executes(this::acceptProposal))
                .then(Commands.literal("deny").executes(this::denyProposal))
                .then(Commands.argument(PLAYER_ARGUMENT, EntityArgument.player()).executes(this::sendProposal)));

        dispatcher.register(Commands.literal("divorce").executes(this::divorce));

        dispatcher.register(Commands.literal("heartcolor")
                .executes(this::heartColorUsage)
                .then(Commands.argument(COLOR_ARGUMENT, StringArgumentType.word())
                        .suggests((context, builder) ->
                                SharedSuggestionProvider.suggest(settings.heart.colors.keySet(), builder))
                        .executes(this::changeHeartColor)));
    }

    private int marryUsage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        messages.send(context.getSource().getPlayerOrException(), "usage-marry");
        return Command.SINGLE_SUCCESS;
    }

    private int heartColorUsage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        messages.send(context.getSource().getPlayerOrException(), "usage-heartcolor", "{colors}", colorNames());
        return Command.SINGLE_SUCCESS;
    }

    private int sendProposal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, PLAYER_ARGUMENT);

        if (target.getUUID().equals(player.getUUID())) {
            messages.send(player, "self-marry");
            return Command.SINGLE_SUCCESS;
        }
        if (marriages.isMarried(player.getUUID())) {
            messages.send(player, "already-married-self");
            return Command.SINGLE_SUCCESS;
        }
        if (marriages.isMarried(target.getUUID())) {
            messages.send(player, "already-married-other", "{player}", nameOf(target));
            return Command.SINGLE_SUCCESS;
        }

        int seconds = settings.proposal.expireSeconds;
        long expiresAt = System.currentTimeMillis() + seconds * 1000L;
        proposals.offer(target.getUUID(), new Proposal(player.getUUID(), nameOf(player), expiresAt));

        messages.send(player, "proposal-sent", "{player}", nameOf(target));
        messages.send(target, "proposal-received", "{player}", nameOf(player), "{seconds}", String.valueOf(seconds));
        return Command.SINGLE_SUCCESS;
    }

    private int acceptProposal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayer player = context.getSource().getPlayerOrException();

        Proposal proposal = proposals.take(player.getUUID());
        if (proposal == null || proposal.expired()) {
            messages.send(player, "no-proposal");
            return Command.SINGLE_SUCCESS;
        }
        ServerPlayer proposer = server.getPlayerList().getPlayer(proposal.from());
        if (proposer == null) {
            messages.send(player, "proposer-offline");
            return Command.SINGLE_SUCCESS;
        }
        if (marriages.isMarried(player.getUUID())) {
            messages.send(player, "already-married-self");
            return Command.SINGLE_SUCCESS;
        }
        if (marriages.isMarried(proposer.getUUID())) {
            messages.send(player, "already-married-other", "{player}", nameOf(proposer));
            return Command.SINGLE_SUCCESS;
        }

        marriages.marry(proposer.getUUID(), player.getUUID(), settings.heart.defaultColor);
        messages.send(player, "married", "{player}", nameOf(proposer));
        messages.send(proposer, "married", "{player}", nameOf(player));
        effects.start(player.getUUID());
        effects.start(proposer.getUUID());
        return Command.SINGLE_SUCCESS;
    }

    private int denyProposal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayer player = context.getSource().getPlayerOrException();

        Proposal proposal = proposals.take(player.getUUID());
        if (proposal == null) {
            messages.send(player, "no-proposal");
            return Command.SINGLE_SUCCESS;
        }
        messages.send(player, "proposal-denied-self", "{player}", proposal.fromName());
        ServerPlayer proposer = server.getPlayerList().getPlayer(proposal.from());
        if (proposer != null) {
            messages.send(proposer, "proposal-denied-other", "{player}", nameOf(player));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int divorce(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayer player = context.getSource().getPlayerOrException();

        Marriage marriage = marriages.divorce(player.getUUID());
        if (marriage == null) {
            messages.send(player, "not-married");
            return Command.SINGLE_SUCCESS;
        }
        messages.send(player, "divorced-self");
        ServerPlayer partner = server.getPlayerList().getPlayer(marriage.partnerOf(player.getUUID()));
        if (partner != null) {
            messages.send(partner, "divorced-other", "{player}", nameOf(player));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int changeHeartColor(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayer player = context.getSource().getPlayerOrException();

        Marriage marriage = marriages.marriageOf(player.getUUID());
        if (marriage == null) {
            messages.send(player, "not-married");
            return Command.SINGLE_SUCCESS;
        }
        String color = StringArgumentType.getString(context, COLOR_ARGUMENT).toLowerCase(Locale.ROOT);
        if (!settings.heart.hasColor(color)) {
            messages.send(player, "unknown-color", "{colors}", colorNames());
            return Command.SINGLE_SUCCESS;
        }

        marriages.changeColor(player.getUUID(), color);
        String code = settings.heart.colorCode(color);
        messages.send(player, "color-set", "{color}", code, "{heart}", settings.heart.symbol);

        UUID partnerId = marriage.partnerOf(player.getUUID());
        ServerPlayer partner = server.getPlayerList().getPlayer(partnerId);
        if (partner != null) {
            messages.send(partner, "color-changed-other",
                    "{player}", nameOf(player), "{color}", code, "{heart}", settings.heart.symbol);
        }
        return Command.SINGLE_SUCCESS;
    }

    private String colorNames() {
        return String.join(", ", settings.heart.colors.keySet());
    }

    private String nameOf(ServerPlayer player) {
        return player.getName().getString();
    }
}
