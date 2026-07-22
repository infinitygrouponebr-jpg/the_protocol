package Infinitygroup.the_protocol.compat;

import Infinitygroup.the_protocol.SurvivalProfessionsMod;
import Infinitygroup.the_protocol.config.CommonConfig;
import Infinitygroup.the_protocol.profession.ProfessionManager;
import Infinitygroup.the_protocol.profession.ProfessionType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/** Soft TaCZ compatibility that intentionally uses no TaCZ classes. */
public final class TaczCompat {
    private static final String TACZ_NAMESPACE = "tacz";
    private static final ResourceLocation HEAVY_WEAPON_SLOWDOWN_ID = ResourceLocation.fromNamespaceAndPath(
            SurvivalProfessionsMod.MOD_ID, "tacz_non_shooter_heavy_weapon_slowdown");

    private TaczCompat() {
    }

    public static boolean isTaczLoaded() {
        return CompatManager.isTaczLoaded();
    }

    public static boolean isTaczWeapon(ItemStack stack) {
        return isTaczLoaded() && !stack.isEmpty()
                && TACZ_NAMESPACE.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace());
    }

    public static boolean isHoldingTaczWeapon(LivingEntity entity) {
        return isHoldingTaczWeaponInMainHand(entity) || isHoldingTaczWeaponInOffHand(entity);
    }

    public static boolean isHoldingTaczWeaponInMainHand(LivingEntity entity) {
        return isTaczWeapon(entity.getMainHandItem());
    }

    public static boolean isHoldingTaczWeaponInOffHand(LivingEntity entity) {
        return isTaczWeapon(entity.getOffhandItem());
    }

    /** Fallback classification for soft compat; TaCZ's runtime gun category API is not used in Phase 1. */
    public static boolean isHeavyTaczWeapon(ItemStack stack) {
        if (!isTaczWeapon(stack)) {
            return false;
        }
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);
        return path.contains("rpg") || path.contains("launcher") || path.contains("lmg")
                || path.contains("sniper") || path.contains("heavy") || path.contains("minigun")
                || path.equals("mg") || path.startsWith("mg_") || path.contains("_mg");
    }

    public static boolean isShooter(ServerPlayer player) {
        return ProfessionManager.get(player).profession() == ProfessionType.SHOOTER;
    }

    public static boolean isEnabled() {
        return isTaczLoaded() && CommonConfig.ENABLE_TACZ_COMPAT.get();
    }

    public static void refreshHeavyWeaponMovementPenalty(ServerPlayer player) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) {
            return;
        }

        movementSpeed.removeModifier(HEAVY_WEAPON_SLOWDOWN_ID);
        if (!isEnabled() || !isHeavyTaczWeapon(player.getMainHandItem())) {
            return;
        }

        double penalty = isShooter(player)
                ? CommonConfig.TACZ_HEAVY_WEAPON_MOVEMENT_PENALTY.get()
                : (CommonConfig.TACZ_ENABLE_NON_SHOOTER_PENALTY.get()
                ? CommonConfig.TACZ_NON_SHOOTER_HEAVY_WEAPON_PENALTY.get() : 0.0D);
        if (penalty > 0.0D) {
            movementSpeed.addTransientModifier(new AttributeModifier(
                    HEAVY_WEAPON_SLOWDOWN_ID,
                    -penalty,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
}
