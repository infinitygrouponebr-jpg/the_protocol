package Infinitygroup.the_protocol.trait;

import Infinitygroup.the_protocol.Config;
import Infinitygroup.the_protocol.data.ModAttachments;
import Infinitygroup.the_protocol.data.PlayerTraitData;
import Infinitygroup.the_protocol.trait.impl.ParkourSpecialistTrait;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TraitService {
    private static final long PARKOUR_ROLL_NOT_USED_TICK = -1L;
    private static final Map<UUID, ServerBossEvent> PARKOUR_STAMINA_BARS = new ConcurrentHashMap<>();

    private TraitService() {
    }

    public static PlayerTraitData getData(ServerPlayer player) {
        return player.getData(ModAttachments.PLAYER_TRAIT_DATA.get());
    }

    public static void setData(ServerPlayer player, PlayerTraitData data) {
        player.setData(ModAttachments.PLAYER_TRAIT_DATA.get(), data);
    }

    public static Optional<TraitDefinition> currentTrait(ServerPlayer player) {
        PlayerTraitData data = getData(player);
        return data.traitLocation() == null ? Optional.empty() : TraitRegistry.get(data.traitLocation());
    }

    public static void handlePlayerLogin(ServerPlayer player) {
        if (player == null || !Config.traitsEnabled()) {
            removeParkourStaminaBar(player);
            return;
        }

        PlayerTraitData data = getData(player);
        if (!data.initialGranted()) {
            if (data.hasTrait()) {
                PlayerTraitData updated = normalizeParkourData(data.withInitialGranted(true));
                setData(player, updated);
                TraitRegistry.get(data.traitLocation()).ifPresent(trait -> trait.onGranted(player, getData(player)));
                refreshPassiveEffects(player);
                return;
            }

            if (!Config.randomInitialTraitEnabled()) {
                return;
            }

            Optional<TraitDefinition> selection = selectRandomTrait(player, null);
            if (selection.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.the_protocol.trait.no_available_traits").withStyle(ChatFormatting.RED), true);
                return;
            }

            grantTrait(player, selection.get(), false);
            return;
        }

        refreshPassiveEffects(player);
    }

    public static void handlePlayerRespawn(ServerPlayer player) {
        if (player == null || !Config.traitsEnabled()) {
            removeParkourStaminaBar(player);
            return;
        }

        removeParkourStaminaBar(player);
        Optional<TraitDefinition> trait = currentTrait(player);
        if (trait.map(value -> value instanceof ParkourSpecialistTrait).orElse(false)) {
            setData(player, normalizeParkourData(getData(player)).withParkourStamina(Config.parkourMaxStamina()));
        }
        refreshPassiveEffects(player);
    }

    public static void handlePlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            removeParkourStaminaBar(player);
        }
    }

    public static void handlePlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || !Config.traitsEnabled()) {
            return;
        }

        if (currentTrait(player).map(trait -> trait instanceof ParkourSpecialistTrait).orElse(false)) {
            regenerateParkourStamina(player);
            syncParkourStaminaBar(player);
        } else {
            removeParkourStaminaBar(player);
        }
    }

    public static void handleLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || !Config.traitsEnabled()) {
            return;
        }

        Optional<TraitDefinition> trait = currentTrait(player);
        if (trait.isEmpty()) {
            return;
        }

        PlayerTraitData data = getData(player);
        if (trait.get() instanceof ParkourSpecialistTrait parkourTrait) {
            parkourTrait.handleFall(player, event, data);
        }
    }

    public static void handleArrowLoose(ArrowLooseEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || !Config.traitsEnabled()) {
            return;
        }

        Optional<TraitDefinition> trait = currentTrait(player);
        if (trait.isEmpty()) {
            return;
        }

        trait.get().onArrowLoose(player, event, getData(player));
    }

    public static Optional<TraitDefinition> selectRandomTrait(ServerPlayer player, ResourceLocation avoidTrait) {
        List<TraitDefinition> candidates = availableTraits();
        if (avoidTrait != null) {
            candidates = candidates.stream().filter(trait -> !trait.id().equals(avoidTrait)).toList();
        }

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        int index = player.getRandom().nextInt(candidates.size());
        return Optional.of(candidates.get(index));
    }

    public static List<TraitDefinition> availableTraits() {
        List<TraitDefinition> traits = new ArrayList<>();
        if (!Config.traitsEnabled()) {
            return traits;
        }

        for (TraitDefinition trait : TraitRegistry.allTraits()) {
            if (Config.isTraitExplicitlyEnabled(trait.id())) {
                traits.add(trait);
            }
        }

        return traits;
    }

    public static void grantTrait(ServerPlayer player, TraitDefinition trait, boolean reroll) {
        PlayerTraitData current = getData(player);
        PlayerTraitData updated = current.withTrait(trait.id())
                .withInitialGranted(true)
                .withRerollUsed(reroll || current.rerollUsed());
        if (trait instanceof ParkourSpecialistTrait) {
            updated = resetParkourState(updated);
        }
        setData(player, updated);
        trait.onGranted(player, updated);
        refreshPassiveEffects(player);

        MutableComponent message = Component.translatable(reroll ? "message.the_protocol.trait.rerolled" : "message.the_protocol.trait.assigned", trait.displayName())
                .withStyle(ChatFormatting.GOLD);
        player.displayClientMessage(message, true);
    }

    public static boolean rerollTrait(ServerPlayer player) {
        if (!Config.rerollEnabled()) {
            player.displayClientMessage(Component.translatable("message.the_protocol.trait.reroll_disabled").withStyle(ChatFormatting.RED), false);
            return false;
        }

        PlayerTraitData current = getData(player);
        if (!current.initialGranted()) {
            player.displayClientMessage(Component.translatable("message.the_protocol.trait.no_current_trait").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (current.rerollUsed()) {
            player.displayClientMessage(Component.translatable("message.the_protocol.trait.reroll_used").withStyle(ChatFormatting.RED), false);
            return false;
        }

        ResourceLocation currentTraitId = current.traitLocation();
        List<TraitDefinition> candidates = availableTraits().stream()
                .filter(trait -> currentTraitId == null || !trait.id().equals(currentTraitId))
                .toList();

        if (candidates.size() <= 0) {
            player.displayClientMessage(Component.translatable("message.the_protocol.trait.no_reroll_available").withStyle(ChatFormatting.RED), false);
            return false;
        }

        TraitDefinition chosen = candidates.get(player.getRandom().nextInt(candidates.size()));
        setData(player, current.withRerollUsed(true).withTrait(chosen.id()).withInitialGranted(true));
        chosen.onGranted(player, getData(player));
        refreshPassiveEffects(player);

        player.displayClientMessage(Component.translatable("message.the_protocol.trait.rerolled", chosen.displayName()).withStyle(ChatFormatting.GREEN), true);
        return true;
    }

    public static boolean setTrait(ServerPlayer player, ResourceLocation traitId) {
        Optional<TraitDefinition> trait = TraitRegistry.get(traitId);
        if (trait.isEmpty()) {
            return false;
        }

        PlayerTraitData current = getData(player);
        PlayerTraitData updated = current.withTrait(traitId).withInitialGranted(true);
        if (trait.get() instanceof ParkourSpecialistTrait) {
            updated = resetParkourState(updated);
        }
        setData(player, updated);
        trait.get().onGranted(player, getData(player));
        refreshPassiveEffects(player);
        return true;
    }

    public static void clearTrait(ServerPlayer player) {
        setData(player, PlayerTraitData.empty());
        refreshPassiveEffects(player);
    }

    public static void refreshPassiveEffects(ServerPlayer player) {
        if (player == null || !Config.traitsEnabled()) {
            removeParkourStaminaBar(player);
            return;
        }

        removeManagedModifiers(player);

        Optional<TraitDefinition> current = currentTrait(player);
        current.ifPresent(trait -> trait.applyPersistentEffects(player, getData(player)));
        syncParkourStaminaBar(player);
    }

    private static void removeManagedModifiers(ServerPlayer player) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null && movementSpeed.getModifier(ParkourSpecialistTrait.SPEED_MODIFIER_ID) != null) {
            movementSpeed.removeModifier(ParkourSpecialistTrait.SPEED_MODIFIER_ID);
        }
    }

    public static void applyParkourSpeedBonus(ServerPlayer player) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null || movementSpeed.getModifier(ParkourSpecialistTrait.SPEED_MODIFIER_ID) != null) {
            return;
        }

        movementSpeed.addTransientModifier(new AttributeModifier(ParkourSpecialistTrait.SPEED_MODIFIER_ID, Config.parkourMoveSpeedBonus(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    public static boolean tryTriggerParkourRoll(ServerPlayer player) {
        if (player == null || !Config.traitsEnabled() || !currentTrait(player).map(trait -> trait instanceof ParkourSpecialistTrait).orElse(false)) {
            return false;
        }

        if (!player.onGround() || player.isPassenger() || player.isFallFlying() || player.isSwimming()) {
            player.displayClientMessage(Component.translatable("message.the_protocol.trait.parkour_roll_ground").withStyle(ChatFormatting.RED), true);
            return false;
        }

        PlayerTraitData data = getData(player);
        long now = player.level().getGameTime();
        long lastRollTick = data.lastParkourRollTick();
        if (lastRollTick >= 0L && now - lastRollTick < Config.parkourRollCooldownTicks()) {
            player.displayClientMessage(Component.translatable("message.the_protocol.trait.parkour_roll_cooldown").withStyle(ChatFormatting.RED), true);
            return false;
        }

        double cost = Config.parkourRollStaminaCost();
        if (data.parkourStamina() < cost) {
            player.displayClientMessage(Component.translatable("message.the_protocol.trait.parkour_roll_stamina").withStyle(ChatFormatting.RED), true);
            return false;
        }

        Vec3 forward = player.getLookAngle();
        Vec3 horizontal = new Vec3(forward.x, 0.0D, forward.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            horizontal = horizontal.normalize();
        }

        Vec3 currentMotion = player.getDeltaMovement();
        Vec3 boosted = currentMotion.add(horizontal.scale(Config.parkourRollForwardStrength()));
        player.setDeltaMovement(boosted.x, Math.min(boosted.y + Config.parkourRollVerticalBoost(), 0.6D), boosted.z);
        player.resetFallDistance();

        setData(player, data.withLastParkourRollTick(now).withParkourStamina(Math.max(0.0D, data.parkourStamina() - cost)));
        syncParkourStaminaBar(player);
        player.displayClientMessage(Component.translatable("message.the_protocol.trait.parkour_rolled").withStyle(ChatFormatting.GREEN), true);
        return true;
    }

    public static void regenerateParkourStamina(ServerPlayer player) {
        if (player == null) {
            return;
        }

        PlayerTraitData data = getData(player);
        double maxStamina = Config.parkourMaxStamina();
        if (data.parkourStamina() >= maxStamina) {
            return;
        }

        double restored = Math.min(maxStamina, data.parkourStamina() + Config.parkourStaminaRegenPerTick());
        if (restored != data.parkourStamina()) {
            setData(player, data.withParkourStamina(restored));
        }
    }

    private static void syncParkourStaminaBar(ServerPlayer player) {
        if (player == null || !Config.traitsEnabled() || !currentTrait(player).map(trait -> trait instanceof ParkourSpecialistTrait).orElse(false)) {
            removeParkourStaminaBar(player);
            return;
        }

        PlayerTraitData data = getData(player);
        ServerBossEvent bar = PARKOUR_STAMINA_BARS.computeIfAbsent(player.getUUID(), uuid -> new ServerBossEvent(
                Component.translatable("message.the_protocol.trait.parkour_stamina"),
                BossEvent.BossBarColor.GREEN,
                BossEvent.BossBarOverlay.PROGRESS
        ));

        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }

        bar.setProgress((float) Math.max(0.0D, Math.min(1.0D, data.parkourStamina() / Math.max(1.0D, Config.parkourMaxStamina()))));
    }

    private static void removeParkourStaminaBar(ServerPlayer player) {
        if (player == null) {
            return;
        }

        ServerBossEvent bar = PARKOUR_STAMINA_BARS.remove(player.getUUID());
        if (bar != null) {
            bar.removePlayer(player);
        }
    }

    private static PlayerTraitData normalizeParkourData(PlayerTraitData data) {
        if (data.lastParkourRollTick() < 0L) {
            return data.withLastParkourRollTick(PARKOUR_ROLL_NOT_USED_TICK);
        }

        return data;
    }

    private static PlayerTraitData resetParkourState(PlayerTraitData data) {
        return normalizeParkourData(data)
                .withLastParkourRollTick(PARKOUR_ROLL_NOT_USED_TICK)
                .withParkourStamina(Config.parkourMaxStamina());
    }

    public static void giveMechanicStarterKit(ServerPlayer player) {
        if (!Config.mechanicStarterKitEnabled()) {
            return;
        }

        for (var item : Config.mechanicStarterKitItems()) {
            ItemStack stack = new ItemStack(item);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }
}
