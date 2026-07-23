package Infinitygroup.the_protocol.compat;

import Infinitygroup.the_protocol.SurvivalProfessionsMod;
import Infinitygroup.the_protocol.config.CommonConfig;
import Infinitygroup.the_protocol.profession.ProfessionManager;
import Infinitygroup.the_protocol.profession.ProfessionType;
import net.combat_roll.api.CombatRoll;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.fml.ModList;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Restricts Combat Roll through its public COUNT attribute. Combat Roll retains ownership of
 * input, movement, animation, sounds, particles, cooldowns, hunger, HUD, and networking.
 */
public final class CombatRollCompat {
    private static final ResourceLocation ROLL_BLOCKER_ID = ResourceLocation.fromNamespaceAndPath(
            SurvivalProfessionsMod.MOD_ID, "non_parkour_roll_block");
    private static final double ROLL_BLOCKER_AMOUNT = -1024.0D;
    private static final AtomicBoolean MISSING_ATTRIBUTE_WARNING_LOGGED = new AtomicBoolean();

    private CombatRollCompat() {
    }

    public static boolean isAvailable() {
        return ModList.get().isLoaded("combat_roll");
    }

    public static boolean shouldRestrictRoll() {
        return isAvailable()
                && CommonConfig.ENABLE_PROFESSION_SYSTEM.get()
                && CommonConfig.ENABLE_MOVEMENT_PERKS.get()
                && CommonConfig.ENABLE_COMBAT_ROLL_COMPAT.get()
                && CommonConfig.RESTRICT_COMBAT_ROLL_TO_PARKOUR.get();
    }

    public static boolean canRoll(ServerPlayer player) {
        return shouldRestrictRoll() && ProfessionManager.get(player).profession() == ProfessionType.PARKOUR;
    }

    /** Uses the shared profession-feedback cooldown after the server validates a denied roll input. */
    public static void notifyRollDenied(ServerPlayer player) {
        TaczCompat.sendProfessionDeniedMessage(player, "message.the_protocol.combat_roll.denied");
    }

    public static void refreshRollPermission(ServerPlayer player) {
        if (!isAvailable()) {
            return;
        }

        AttributeInstance count = player.getAttribute(CombatRoll.Attributes.COUNT.entry);
        if (count == null) {
            if (MISSING_ATTRIBUTE_WARNING_LOGGED.compareAndSet(false, true)) {
                SurvivalProfessionsMod.LOGGER.warn("Combat Roll COUNT attribute is unavailable; roll permissions cannot be updated.");
            }
            return;
        }

        count.removeModifier(ROLL_BLOCKER_ID);
        if (shouldRestrictRoll() && !canRoll(player)) {
            count.addTransientModifier(new AttributeModifier(
                    ROLL_BLOCKER_ID,
                    ROLL_BLOCKER_AMOUNT,
                    AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void clearRollRestriction(ServerPlayer player) {
        if (!isAvailable()) {
            return;
        }

        AttributeInstance count = player.getAttribute(CombatRoll.Attributes.COUNT.entry);
        if (count != null) {
            count.removeModifier(ROLL_BLOCKER_ID);
        }
    }
}
