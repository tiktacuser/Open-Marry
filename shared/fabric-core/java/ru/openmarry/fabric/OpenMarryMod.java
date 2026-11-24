package ru.openmarry.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import ru.openmarry.fabric.command.MarriageCommands;
import ru.openmarry.fabric.config.MarriageSettings;
import ru.openmarry.fabric.effects.PartnerProtection;
import ru.openmarry.fabric.effects.PartnerRegeneration;
import ru.openmarry.fabric.effects.WeddingEffects;
import ru.openmarry.fabric.marriage.HeartDisplay;
import ru.openmarry.fabric.marriage.MarriageRegistry;
import ru.openmarry.fabric.marriage.ProposalBook;
import ru.openmarry.fabric.text.Messages;

import java.nio.file.Path;

public final class OpenMarryMod implements ModInitializer {

    @Override
    public void onInitialize() {
        Path configDirectory = FabricLoader.getInstance().getConfigDir();
        MarriageSettings settings = MarriageSettings.load(configDirectory.resolve("openmarry.json"));
        MarriageRegistry marriages = MarriageRegistry.load(configDirectory.resolve("openmarry-data.json"));

        WeddingEffects weddingEffects = new WeddingEffects(settings);
        MarriageCommands commands = new MarriageCommands(
                settings, marriages, new ProposalBook(), new Messages(settings), weddingEffects);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                commands.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(weddingEffects::tick);

        PartnerProtection.register(settings, marriages);
        PartnerRegeneration.register(settings, marriages);
        HeartPlaceholder.register(new HeartDisplay(settings, marriages));
    }
}
