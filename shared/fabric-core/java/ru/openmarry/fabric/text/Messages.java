package ru.openmarry.fabric.text;

import net.minecraft.server.level.ServerPlayer;
import ru.openmarry.fabric.config.MarriageSettings;

public final class Messages {

    private final MarriageSettings settings;

    public Messages(MarriageSettings settings) {
        this.settings = settings;
    }

    public void send(ServerPlayer receiver, String key, String... replacements) {
        String text = settings.message(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace(replacements[i], replacements[i + 1]);
        }
        receiver.sendSystemMessage(LegacyText.parse(settings.message("prefix") + text));
    }
}
