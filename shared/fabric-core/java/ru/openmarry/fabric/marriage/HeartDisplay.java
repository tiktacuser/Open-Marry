package ru.openmarry.fabric.marriage;

import net.minecraft.network.chat.Component;
import ru.openmarry.fabric.config.MarriageSettings;
import ru.openmarry.fabric.text.LegacyText;

import java.util.UUID;

public final class HeartDisplay {

    private final MarriageSettings settings;
    private final MarriageRegistry registry;

    public HeartDisplay(MarriageSettings settings, MarriageRegistry registry) {
        this.settings = settings;
        this.registry = registry;
    }

    public Component of(UUID player) {
        Marriage marriage = registry.marriageOf(player);
        if (marriage == null) {
            return LegacyText.parse(settings.heart.emptyOutput);
        }
        return LegacyText.parse(settings.heart.colorCode(marriage.color()) + settings.heart.symbol);
    }
}
