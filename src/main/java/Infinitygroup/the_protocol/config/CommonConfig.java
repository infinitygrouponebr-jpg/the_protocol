package Infinitygroup.the_protocol.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_PROFESSION_SYSTEM = BUILDER.define("enableProfessionSystem", true);
    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_COMMANDS = BUILDER.define("enableDebugCommands", true);
    public static final ModConfigSpec.BooleanValue ENABLE_TACZ_COMPAT = BUILDER.define("enableTaczCompat", true);
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
