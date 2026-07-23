package Infinitygroup.the_protocol.compat;

import Infinitygroup.the_protocol.SurvivalProfessionsMod;
import Infinitygroup.the_protocol.config.CommonConfig;
import Infinitygroup.the_protocol.profession.ProfessionManager;
import Infinitygroup.the_protocol.profession.ProfessionType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Optional TaCZ integration. All TaCZ access is reflective so this mod remains loadable without TaCZ.
 * The inspected 1.21.1 API supplies mutable INACCURACY cache data per shooter; it supplies no equivalent
 * public, per-player mutation point for recoil or reload duration.
 */
public final class TaczCompat {
    private static final String TACZ_NAMESPACE = "tacz";
    private static final String IGUN = "com.tacz.guns.api.item.IGun";
    private static final String TIMELESS_API = "com.tacz.guns.api.TimelessAPI";
    private static final String BULLET = "com.tacz.guns.entity.EntityKineticBullet";
    private static final ResourceLocation HEAVY_WEAPON_SLOWDOWN_ID = ResourceLocation.fromNamespaceAndPath(
            SurvivalProfessionsMod.MOD_ID, "tacz_non_shooter_heavy_weapon_slowdown");
    private static final Map<Object, Double> ACCURACY_FACTORS = new WeakHashMap<>();
    private static final Map<UUID, Long> DENIAL_MESSAGE_TICKS = new HashMap<>();
    private static boolean initialized;

    private TaczCompat() {
    }

    public static void initialize() {
        if (initialized || !isTaczLoaded()) {
            return;
        }
        initialized = true;
        try {
            Class<?> eventType = Class.forName("com.tacz.guns.api.event.common.AttachmentPropertyEvent");
            Consumer<Event> listener = event -> applyAccuracyModifier(event);
            NeoForge.EVENT_BUS.addListener((Class) eventType, (Consumer) listener);
            SurvivalProfessionsMod.LOGGER.info("TaCZ 1.21.1 accuracy compatibility enabled through AttachmentPropertyEvent");
        } catch (ReflectiveOperationException | LinkageError exception) {
            SurvivalProfessionsMod.LOGGER.warn("TaCZ detected, but its optional accuracy hook is unavailable", exception);
        }
    }

    public static boolean isTaczLoaded() {
        return CompatManager.isTaczLoaded();
    }

    public static boolean isTaczWeapon(ItemStack stack) {
        if (!isTaczLoaded() || stack.isEmpty()) {
            return false;
        }
        try {
            Class<?> gunClass = Class.forName(IGUN);
            Object gun = gunClass.getMethod("getIGunOrNull", ItemStack.class).invoke(null, stack);
            if (gun != null) {
                return true;
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Older/incompatible TaCZ builds retain the conservative registry fallback below.
        }
        return TACZ_NAMESPACE.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace());
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

    public static TaczWeaponCategory getWeaponCategory(ItemStack stack) {
        if (!isTaczWeapon(stack)) {
            return TaczWeaponCategory.UNKNOWN;
        }
        try {
            Class<?> gunClass = Class.forName(IGUN);
            Object gun = gunClass.getMethod("getIGunOrNull", ItemStack.class).invoke(null, stack);
            if (gun != null) {
                ResourceLocation gunId = (ResourceLocation) gunClass.getMethod("getGunId", ItemStack.class).invoke(gun, stack);
                if (gunId != null) {
                    Class<?> api = Class.forName(TIMELESS_API);
                    Optional<?> index = (Optional<?>) api.getMethod("getCommonGunIndex", ResourceLocation.class).invoke(null, gunId);
                    if (index.isPresent()) {
                        String type = (String) index.get().getClass().getMethod("getType").invoke(index.get());
                        TaczWeaponCategory category = categoryFrom(type);
                        if (category != TaczWeaponCategory.UNKNOWN) {
                            return category;
                        }
                    }
                }
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Gun pack data can be unavailable during early load; use the documented ID fallback.
        }
        return categoryFrom(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
    }

    public static boolean isHeavyTaczWeapon(ItemStack stack) {
        return switch (getWeaponCategory(stack)) {
            case SNIPER, LMG, LAUNCHER, HEAVY -> true;
            default -> false;
        };
    }

    public static boolean isDamageFromTaczShot(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct == null || !isTaczLoaded()) {
            return false;
        }
        try {
            if (Class.forName(BULLET).isInstance(direct)) {
                return true;
            }
        } catch (ClassNotFoundException | LinkageError ignored) {
            // Continue with the entity-type namespace check for compatible ports.
        }
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(direct.getType());
        return entityId != null && TACZ_NAMESPACE.equals(entityId.getNamespace())
                && direct.getClass().getPackageName().startsWith("com.tacz.guns.");
    }

    /** Returns the player that actually owns a confirmed TaCZ projectile, if any. */
    public static ServerPlayer getTaczShotAttacker(DamageSource source) {
        if (!isDamageFromTaczShot(source)) {
            return null;
        }
        return source.getEntity() instanceof ServerPlayer player ? player : null;
    }

    public static boolean isShooter(ServerPlayer player) {
        return ProfessionManager.get(player).profession() == ProfessionType.SHOOTER;
    }

    /** Server-authoritative permission for TaCZ attachment and gunsmith-table modifications. */
    public static boolean canModifyTaczWeapons(ServerPlayer player) {
        return !CommonConfig.TACZ_RESTRICT_ATTACHMENTS_TO_SHOOTER.get() || (isEnabled() && isShooter(player));
    }

    /** Server-authoritative permission for the TaCZ weapon-bash action. */
    public static boolean canUseTaczMelee(ServerPlayer player) {
        return !CommonConfig.TACZ_RESTRICT_MELEE_TO_SHOOTER.get() || (isEnabled() && isShooter(player));
    }

    public static boolean shouldShowDeniedMessages() {
        return CommonConfig.TACZ_SHOW_DENIED_MESSAGES.get();
    }

    /** Sends a translated, rate-limited denial only after an action was rejected on the server. */
    public static void sendProfessionDeniedMessage(ServerPlayer player, String translationKey) {
        if (!shouldShowDeniedMessages()) {
            return;
        }
        long gameTime = player.level().getGameTime();
        UUID playerId = player.getUUID();
        synchronized (DENIAL_MESSAGE_TICKS) {
            long previous = DENIAL_MESSAGE_TICKS.getOrDefault(playerId, Long.MIN_VALUE);
            if (gameTime - previous < 20L) {
                return;
            }
            DENIAL_MESSAGE_TICKS.put(playerId, gameTime);
            if (DENIAL_MESSAGE_TICKS.size() > 256) {
                DENIAL_MESSAGE_TICKS.entrySet().removeIf(entry -> gameTime - entry.getValue() > 200L);
            }
        }
        player.displayClientMessage(Component.translatable(translationKey), true);
    }

    public static void notifyTaczModificationDenied(ServerPlayer player) {
        sendProfessionDeniedMessage(player, "message.the_protocol.tacz.modify_denied");
    }

    public static void notifyMeleeDenied(ServerPlayer player) {
        sendProfessionDeniedMessage(player, "message.the_protocol.tacz.melee_denied");
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
        double penalty = isShooter(player) ? CommonConfig.TACZ_HEAVY_WEAPON_MOVEMENT_PENALTY.get()
                : (CommonConfig.TACZ_ENABLE_NON_SHOOTER_PENALTY.get()
                ? CommonConfig.TACZ_NON_SHOOTER_HEAVY_WEAPON_PENALTY.get() : 0.0D);
        if (penalty > 0.0D) {
            movementSpeed.addTransientModifier(new AttributeModifier(HEAVY_WEAPON_SLOWDOWN_ID, -penalty,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void applyAccuracyModifier(Object event) {
        if (!isEnabled()) {
            return;
        }
        try {
            Object shooter = event.getClass().getMethod("getShooter").invoke(event);
            if (!(shooter instanceof ServerPlayer player)) {
                return; // Server is authoritative for TaCZ projectile spread.
            }
            double multiplier = isShooter(player) ? CommonConfig.TACZ_SHOOTER_ACCURACY_MULTIPLIER.get()
                    : (CommonConfig.TACZ_ENABLE_NON_SHOOTER_PENALTY.get()
                    ? CommonConfig.TACZ_NON_SHOOTER_ACCURACY_MULTIPLIER.get() : 1.0D);
            if (multiplier <= 0.0D) {
                return;
            }
            Object cache = event.getClass().getMethod("getCacheProperty").invoke(event);
            synchronized (ACCURACY_FACTORS) {
                double previous = ACCURACY_FACTORS.getOrDefault(cache, 1.0D);
                if (Double.compare(previous, multiplier) == 0) {
                    return;
                }
                Class<?> properties = Class.forName("com.tacz.guns.api.GunProperties");
                Field propertyField = properties.getField("INACCURACY");
                Object property = propertyField.get(null);
                Class<?> gunProperty = Class.forName("com.tacz.guns.api.GunProperty");
                Method getCache = cache.getClass().getMethod("getCache", gunProperty);
                Object original = getCache.invoke(cache, property);
                if (!(original instanceof Map<?, ?> values)) {
                    return;
                }
                Map<Object, Float> adjusted = new HashMap<>();
                double correction = previous / multiplier; // effectiveInaccuracy = baseInaccuracy / accuracyMultiplier
                for (Map.Entry<?, ?> entry : values.entrySet()) {
                    if (entry.getValue() instanceof Number value) {
                        adjusted.put(entry.getKey(), (float) (value.floatValue() * correction));
                    }
                }
                cache.getClass().getMethod("setCache", gunProperty, Object.class).invoke(cache, property, adjusted);
                ACCURACY_FACTORS.put(cache, multiplier);
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            SurvivalProfessionsMod.LOGGER.debug("Could not apply TaCZ per-player inaccuracy modifier", exception);
        }
    }

    private static TaczWeaponCategory categoryFrom(String value) {
        String type = value == null ? "" : value.toLowerCase(Locale.ROOT);
        if (type.contains("pistol") || type.contains("handgun")) return TaczWeaponCategory.PISTOL;
        if (type.contains("smg") || type.contains("submachine")) return TaczWeaponCategory.SMG;
        if (type.contains("shotgun")) return TaczWeaponCategory.SHOTGUN;
        if (type.contains("sniper") || type.contains("marksman")) return TaczWeaponCategory.SNIPER;
        if (type.contains("lmg") || type.contains("minigun") || type.matches(".*(^|_)mg(_|$).*")) return TaczWeaponCategory.LMG;
        if (type.contains("launcher") || type.contains("rpg") || type.contains("grenade")) return TaczWeaponCategory.LAUNCHER;
        if (type.contains("heavy")) return TaczWeaponCategory.HEAVY;
        if (type.contains("rifle") || type.contains("carbine") || type.contains("ar_")) return TaczWeaponCategory.RIFLE;
        return TaczWeaponCategory.UNKNOWN;
    }
}
