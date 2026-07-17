package Infinitygroup.the_protocol.trait;

import Infinitygroup.the_protocol.data.PlayerTraitData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;

import java.util.Set;

public interface TraitDefinition {
    ResourceLocation id();

    Component displayName();

    Component description();

    default Set<String> optionalCompatMods() {
        return Set.of();
    }

    default void onGranted(ServerPlayer player, PlayerTraitData data) {
    }

    default void applyPersistentEffects(ServerPlayer player, PlayerTraitData data) {
    }

    default void onLivingFall(ServerPlayer player, LivingFallEvent event, PlayerTraitData data) {
    }

    default void onArrowLoose(ServerPlayer player, ArrowLooseEvent event, PlayerTraitData data) {
    }
}
