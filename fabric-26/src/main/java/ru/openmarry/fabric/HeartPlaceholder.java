package ru.openmarry.fabric;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import ru.openmarry.fabric.marriage.HeartDisplay;

public final class HeartPlaceholder {

    private HeartPlaceholder() {
    }

    public static void register(HeartDisplay display) {
        Placeholders.registerCommon(Identifier.fromNamespaceAndPath("hug", "heart"), (context, argument) -> {
            if (!context.hasPlayer()) {
                return PlaceholderResult.value(Component.empty());
            }
            return PlaceholderResult.value(display.of(context.getPlayer().getUUID()));
        });
    }
}
