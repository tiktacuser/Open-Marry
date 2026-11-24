package ru.openmarry.fabric.effects;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import ru.openmarry.fabric.config.MarriageSettings;
import ru.openmarry.fabric.marriage.Marriage;
import ru.openmarry.fabric.marriage.MarriageRegistry;

public final class PartnerRegeneration {

    private PartnerRegeneration() {
    }

    public static void register(MarriageSettings settings, MarriageRegistry marriages) {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MarriageSettings.Regeneration rules = settings.regeneration;
            if (!rules.enabled || server.getTickCount() % Math.max(1, rules.intervalTicks) != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                applyIfPartnerNearby(server, marriages, rules, player);
            }
        });
    }

    private static void applyIfPartnerNearby(MinecraftServer server, MarriageRegistry marriages,
                                             MarriageSettings.Regeneration rules, ServerPlayer player) {
        Marriage marriage = marriages.marriageOf(player.getUUID());
        if (marriage == null) {
            return;
        }
        ServerPlayer partner = server.getPlayerList().getPlayer(marriage.partnerOf(player.getUUID()));
        if (partner == null || partner.level() != player.level()) {
            return;
        }
        if (player.distanceToSqr(partner) > rules.radius * rules.radius) {
            return;
        }
        int amplifier = Math.max(0, rules.level - 1);
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, rules.durationTicks, amplifier, false, false, true));
    }
}
