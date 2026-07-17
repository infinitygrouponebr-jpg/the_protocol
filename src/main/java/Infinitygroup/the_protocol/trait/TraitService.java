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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TraitService {
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
            return;
        }

        PlayerTraitData data = getData(player);
        if (!data.initialGranted()) {
            if (data.hasTrait()) {
                setData(player, data.withInitialGranted(true));
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
            return;
        }

        refreshPassiveEffects(player);
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
        setData(player, current.withTrait(traitId).withInitialGranted(true));
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
            return;
        }

        removeManagedModifiers(player);

        Optional<TraitDefinition> current = currentTrait(player);
        current.ifPresent(trait -> trait.applyPersistentEffects(player, getData(player)));
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
