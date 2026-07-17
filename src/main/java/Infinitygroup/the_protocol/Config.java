package Infinitygroup.the_protocol;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    private static final ModConfigSpec.BooleanValue ENABLE_TRAITS = BUILDER
            .comment("Master switch for the trait system.")
            .define("enableTraits", true);

    private static final ModConfigSpec.BooleanValue ENABLE_RANDOM_INITIAL_TRAIT = BUILDER
            .comment("If true, players receive a random trait the first time they join a world.")
            .define("enableRandomInitialTrait", true);

    private static final ModConfigSpec.BooleanValue ENABLE_REROLL = BUILDER
            .comment("If true, players can use the reroll command once.")
            .define("enableReroll", true);

    private static final ModConfigSpec.BooleanValue ALLOW_OPTIONAL_MOD_COMPAT = BUILDER
            .comment("If true, traits keep optional compat hooks enabled when their target mods are present.")
            .define("allowOptionalModCompat", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> ENABLED_TRAIT_IDS = BUILDER
            .comment("List of enabled trait ids. Leave empty to allow every registered trait.")
            .defineListAllowEmpty("enabledTraitIds", List::<String>of, () -> "", Config::validateResourceLocation);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> VEHICLE_COMPAT_MOD_IDS = BUILDER
            .comment("Optional mod ids that should be treated as vehicle compatibility targets.")
            .defineListAllowEmpty("vehicleCompatModIds", List::<String>of, () -> "", Config::validateModId);

    private static final ModConfigSpec.IntValue GUN_BOW_CHARGE_REDUCTION_TICKS = BUILDER
            .comment("How many bow draw ticks the Gun Specialist trims from a shot.")
            .defineInRange("gunBowChargeReductionTicks", 1, 0, 20);

    private static final ModConfigSpec.DoubleValue GUN_EXTRA_ACCURACY = BUILDER
            .comment("Generic extra handling bonus for Gun Specialist bows/crossbows.")
            .defineInRange("gunExtraAccuracy", 0.05D, 0.0D, 1.0D);

    private static final ModConfigSpec.DoubleValue PARKOUR_FALL_DAMAGE_MULTIPLIER = BUILDER
            .comment("Base fall damage multiplier for Parkour Specialist.")
            .defineInRange("parkourFallDamageMultiplier", 0.70D, 0.0D, 1.0D);

    private static final ModConfigSpec.DoubleValue PARKOUR_ROLL_DAMAGE_MULTIPLIER = BUILDER
            .comment("Damage multiplier applied when a parkour roll triggers.")
            .defineInRange("parkourRollDamageMultiplier", 0.20D, 0.0D, 1.0D);

    private static final ModConfigSpec.DoubleValue PARKOUR_ROLL_MIN_DISTANCE = BUILDER
            .comment("Minimum fall distance required to trigger a roll.")
            .defineInRange("parkourRollMinDistance", 3.0D, 0.0D, 100.0D);

    private static final ModConfigSpec.IntValue PARKOUR_ROLL_COOLDOWN_TICKS = BUILDER
            .comment("Cooldown in ticks before the next roll can trigger.")
            .defineInRange("parkourRollCooldownTicks", 20, 0, 20 * 60 * 5);

    private static final ModConfigSpec.DoubleValue PARKOUR_MOVE_SPEED_BONUS = BUILDER
            .comment("Small movement speed bonus for Parkour Specialist.")
            .defineInRange("parkourMoveSpeedBonus", 0.03D, 0.0D, 0.20D);

    private static final ModConfigSpec.BooleanValue MECHANIC_STARTER_KIT_ENABLED = BUILDER
            .comment("If true, Mechanic grants a small starter kit when first assigned.")
            .define("mechanicStarterKitEnabled", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> MECHANIC_STARTER_KIT_ITEMS = BUILDER
            .comment("Starter kit item ids for Mechanic. Each entry gives one item stack.")
            .defineListAllowEmpty("mechanicStarterKitItems", () -> List.of("minecraft:iron_ingot", "minecraft:redstone", "minecraft:compass"), () -> "", Config::validateResourceLocation);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean enableTraits;
    private static boolean enableRandomInitialTrait;
    private static boolean enableReroll;
    private static boolean allowOptionalModCompat;
    private static Set<ResourceLocation> enabledTraitIds = Collections.emptySet();
    private static List<String> vehicleCompatModIds = List.of();
    private static int gunBowChargeReductionTicks;
    private static double gunExtraAccuracy;
    private static double parkourFallDamageMultiplier;
    private static double parkourRollDamageMultiplier;
    private static double parkourRollMinDistance;
    private static int parkourRollCooldownTicks;
    private static double parkourMoveSpeedBonus;
    private static boolean mechanicStarterKitEnabled;
    private static List<Item> mechanicStarterKitItems = List.of();

    private Config() {
    }

    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, SPEC);
    }

    public static void onConfigLoad(final net.neoforged.fml.event.config.ModConfigEvent event) {
        enableTraits = ENABLE_TRAITS.get();
        enableRandomInitialTrait = ENABLE_RANDOM_INITIAL_TRAIT.get();
        enableReroll = ENABLE_REROLL.get();
        allowOptionalModCompat = ALLOW_OPTIONAL_MOD_COMPAT.get();
        enabledTraitIds = ENABLED_TRAIT_IDS.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(resourceLocation -> resourceLocation != null)
                .collect(Collectors.toUnmodifiableSet());
        vehicleCompatModIds = List.copyOf(VEHICLE_COMPAT_MOD_IDS.get());
        gunBowChargeReductionTicks = GUN_BOW_CHARGE_REDUCTION_TICKS.get();
        gunExtraAccuracy = GUN_EXTRA_ACCURACY.get();
        parkourFallDamageMultiplier = PARKOUR_FALL_DAMAGE_MULTIPLIER.get();
        parkourRollDamageMultiplier = PARKOUR_ROLL_DAMAGE_MULTIPLIER.get();
        parkourRollMinDistance = PARKOUR_ROLL_MIN_DISTANCE.get();
        parkourRollCooldownTicks = PARKOUR_ROLL_COOLDOWN_TICKS.get();
        parkourMoveSpeedBonus = PARKOUR_MOVE_SPEED_BONUS.get();
        mechanicStarterKitEnabled = MECHANIC_STARTER_KIT_ENABLED.get();
        mechanicStarterKitItems = MECHANIC_STARTER_KIT_ITEMS.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(resourceLocation -> resourceLocation != null && BuiltInRegistries.ITEM.containsKey(resourceLocation))
                .map(BuiltInRegistries.ITEM::get)
                .collect(Collectors.toCollection(ArrayList::new));

        LOGGER.info("Trait config loaded: enabledTraits={}, reroll={}, randomInitial={}, compatTargets={}",
                enableTraits,
                enableReroll,
                enableRandomInitialTrait,
                vehicleCompatModIds);
    }

    private static boolean validateResourceLocation(final Object value) {
        return value instanceof String string && ResourceLocation.tryParse(string) != null;
    }

    private static boolean validateModId(final Object value) {
        return value instanceof String string && !string.isBlank() && string.chars().allMatch(character -> character == '_' || character == '-' || Character.isLetterOrDigit(character));
    }

    public static boolean traitsEnabled() {
        return enableTraits;
    }

    public static boolean randomInitialTraitEnabled() {
        return enableTraits && enableRandomInitialTrait;
    }

    public static boolean rerollEnabled() {
        return enableTraits && enableReroll;
    }

    public static boolean allowOptionalModCompat() {
        return allowOptionalModCompat;
    }

    public static boolean isTraitExplicitlyEnabled(ResourceLocation traitId) {
        return enabledTraitIds.isEmpty() || enabledTraitIds.contains(traitId);
    }

    public static List<String> vehicleCompatModIds() {
        return vehicleCompatModIds;
    }

    public static int gunBowChargeReductionTicks() {
        return gunBowChargeReductionTicks;
    }

    public static double gunExtraAccuracy() {
        return gunExtraAccuracy;
    }

    public static double parkourFallDamageMultiplier() {
        return parkourFallDamageMultiplier;
    }

    public static double parkourRollDamageMultiplier() {
        return parkourRollDamageMultiplier;
    }

    public static double parkourRollMinDistance() {
        return parkourRollMinDistance;
    }

    public static int parkourRollCooldownTicks() {
        return parkourRollCooldownTicks;
    }

    public static double parkourMoveSpeedBonus() {
        return parkourMoveSpeedBonus;
    }

    public static boolean mechanicStarterKitEnabled() {
        return mechanicStarterKitEnabled;
    }

    public static List<Item> mechanicStarterKitItems() {
        return mechanicStarterKitItems;
    }
}
