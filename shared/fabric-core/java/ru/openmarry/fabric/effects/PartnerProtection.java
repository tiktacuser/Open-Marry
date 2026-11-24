package ru.openmarry.fabric.effects;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import ru.openmarry.fabric.config.MarriageSettings;
import ru.openmarry.fabric.marriage.Marriage;
import ru.openmarry.fabric.marriage.MarriageRegistry;

public final class PartnerProtection {

    private PartnerProtection() {
    }

    public static void register(MarriageSettings settings, MarriageRegistry marriages) {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!settings.protection.blockPartnerDamage) {
                return true;
            }
            if (!(entity instanceof ServerPlayer victim) || !(source.getEntity() instanceof ServerPlayer attacker)) {
                return true;
            }
            Marriage marriage = marriages.marriageOf(victim.getUUID());
            return marriage == null || !marriage.partnerOf(victim.getUUID()).equals(attacker.getUUID());
        });
    }
}
