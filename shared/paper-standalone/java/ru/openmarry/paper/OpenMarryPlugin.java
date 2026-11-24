package ru.openmarry.paper;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.openmarry.paper.command.MarriageCommands;
import ru.openmarry.paper.effects.WeddingEffects;
import ru.openmarry.paper.listener.PartnerProtectionListener;
import ru.openmarry.paper.marriage.HeartPalette;
import ru.openmarry.paper.marriage.MarriageRegistry;
import ru.openmarry.paper.marriage.ProposalBook;
import ru.openmarry.paper.message.Messages;
import ru.openmarry.paper.placeholder.HeartExpansion;
import ru.openmarry.paper.task.PartnerRegeneration;

import java.util.List;

public final class OpenMarryPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        HeartPalette palette = new HeartPalette(this);
        MarriageRegistry marriages = new MarriageRegistry(this, palette);
        MarriageCommands commands = new MarriageCommands(
                this, marriages, new ProposalBook(), palette, new Messages(this), new WeddingEffects(this));

        for (String name : List.of("marry", "divorce", "heartcolor")) {
            PluginCommand command = getCommand(name);
            command.setExecutor(commands);
            command.setTabCompleter(commands);
        }

        getServer().getPluginManager().registerEvents(new PartnerProtectionListener(this, marriages), this);
        PartnerRegeneration.start(this, marriages);
        HeartExpansion.registerIfAvailable(this, marriages);
    }
}
