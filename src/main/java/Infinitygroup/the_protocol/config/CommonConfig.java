package Infinitygroup.the_protocol.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_PROFESSION_SYSTEM = BUILDER.define("enableProfessionSystem", true);
    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_COMMANDS = BUILDER.define("enableDebugCommands", true);
    public static final ModConfigSpec.BooleanValue ENABLE_TACZ_COMPAT = BUILDER.define("enableTaczCompat", true);
    public static final ModConfigSpec.BooleanValue TACZ_RESTRICT_ATTACHMENTS_TO_SHOOTER =
            BUILDER.define("tacz.restrictAttachmentsToShooter", true);
    public static final ModConfigSpec.BooleanValue TACZ_RESTRICT_MELEE_TO_SHOOTER =
            BUILDER.define("tacz.restrictMeleeToShooter", true);
    public static final ModConfigSpec.BooleanValue TACZ_SHOW_DENIED_MESSAGES =
            BUILDER.define("tacz.showDeniedMessages", true);
    public static final ModConfigSpec.BooleanValue TACZ_ENABLE_SHOOTER_BONUS = BUILDER.define("tacz.enableShooterBonus", true);
    public static final ModConfigSpec.BooleanValue TACZ_ENABLE_NON_SHOOTER_PENALTY = BUILDER.define("tacz.enableNonShooterPenalty", true);
    public static final ModConfigSpec.BooleanValue TACZ_ENABLE_XP_GAIN = BUILDER.define("tacz.enableXpGain", true);
    public static final ModConfigSpec.BooleanValue TACZ_ALLOW_NON_SHOOTERS = BUILDER.define("tacz.allowNonShooters", true);
    public static final ModConfigSpec.IntValue TACZ_SHOOTER_XP_ON_HIT = BUILDER.defineInRange("tacz.shooterXpOnHit", 2, 0, 1_000_000);
    public static final ModConfigSpec.IntValue TACZ_SHOOTER_XP_ON_KILL = BUILDER.defineInRange("tacz.shooterXpOnKill", 10, 0, 1_000_000);
    public static final ModConfigSpec.IntValue TACZ_NON_SHOOTER_XP_ON_HIT = BUILDER.defineInRange("tacz.nonShooterXpOnHit", 0, 0, 1_000_000);
    public static final ModConfigSpec.IntValue TACZ_NON_SHOOTER_XP_ON_KILL = BUILDER.defineInRange("tacz.nonShooterXpOnKill", 0, 0, 1_000_000);
    public static final ModConfigSpec.DoubleValue TACZ_SHOOTER_DAMAGE_MULTIPLIER = BUILDER.defineInRange("tacz.shooterDamageMultiplier", 1.10D, 0.0D, 10.0D);
    public static final ModConfigSpec.DoubleValue TACZ_NON_SHOOTER_DAMAGE_MULTIPLIER = BUILDER.defineInRange("tacz.nonShooterDamageMultiplier", 0.55D, 0.0D, 10.0D);
    public static final ModConfigSpec.DoubleValue TACZ_HEAVY_WEAPON_MOVEMENT_PENALTY = BUILDER.defineInRange("tacz.heavyWeaponMovementPenalty", 0.15D, 0.0D, 1.0D);
    public static final ModConfigSpec.DoubleValue TACZ_NON_SHOOTER_HEAVY_WEAPON_PENALTY = BUILDER.defineInRange("tacz.nonShooterHeavyWeaponPenalty", 0.35D, 0.0D, 1.0D);
    // TODO Phase 3: TaCZ 1.21.1 exposes no safe per-player public recoil mutation point.
    public static final ModConfigSpec.DoubleValue TACZ_SHOOTER_RECOIL_MULTIPLIER = BUILDER.defineInRange("tacz.shooterRecoilMultiplier", 0.85D, 0.0D, 10.0D);
    public static final ModConfigSpec.DoubleValue TACZ_NON_SHOOTER_RECOIL_MULTIPLIER = BUILDER.defineInRange("tacz.nonShooterRecoilMultiplier", 1.60D, 0.0D, 10.0D);
    public static final ModConfigSpec.DoubleValue TACZ_SHOOTER_ACCURACY_MULTIPLIER = BUILDER.defineInRange("tacz.shooterAccuracyMultiplier", 1.10D, 0.0D, 10.0D);
    public static final ModConfigSpec.DoubleValue TACZ_NON_SHOOTER_ACCURACY_MULTIPLIER = BUILDER.defineInRange("tacz.nonShooterAccuracyMultiplier", 0.70D, 0.0D, 10.0D);
    // TODO Phase 3: TaCZ 1.21.1 exposes reload lifecycle events but no mutable reload-duration value.
    public static final ModConfigSpec.DoubleValue TACZ_SHOOTER_RELOAD_SPEED_MULTIPLIER = BUILDER.defineInRange("tacz.shooterReloadSpeedMultiplier", 1.10D, 0.0D, 10.0D);
    public static final ModConfigSpec.DoubleValue TACZ_NON_SHOOTER_RELOAD_SPEED_MULTIPLIER = BUILDER.defineInRange("tacz.nonShooterReloadSpeedMultiplier", 0.70D, 0.0D, 10.0D);
    public static final ModConfigSpec.BooleanValue ENABLE_VEHICLE_COMPAT = BUILDER.define("enableVehicleCompat", true);
    public static final ModConfigSpec.BooleanValue ENABLE_MOVEMENT_PERKS = BUILDER.define("enableMovementPerks", true);
    public static final ModConfigSpec.BooleanValue ENABLE_COMBAT_ROLL_COMPAT = BUILDER.define("enableCombatRollCompat", true);
    public static final ModConfigSpec.BooleanValue RESTRICT_COMBAT_ROLL_TO_PARKOUR = BUILDER.define("restrictCombatRollToParkour", true);
    public static final ModConfigSpec.ConfigValue<String> STARTING_PROFESSION = BUILDER.define("startingProfession", "NONE");
    public static final ModConfigSpec.IntValue MAX_PROFESSION_LEVEL = BUILDER.defineInRange("maxProfessionLevel", 50, 1, 1_000);
    public static final ModConfigSpec.IntValue XP_PER_LEVEL_BASE = BUILDER.defineInRange("xpPerLevelBase", 100, 1, 1_000_000);
    public static final ModConfigSpec SPEC = BUILDER.build();

    private CommonConfig() {
    }

    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, SPEC);
    }
}
