package Infinitygroup.the_protocol.trait.impl;

import Infinitygroup.the_protocol.Config;
import Infinitygroup.the_protocol.data.PlayerTraitData;
import Infinitygroup.the_protocol.trait.AbstractTraitDefinition;
import Infinitygroup.the_protocol.trait.TraitService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

import java.util.Set;

public final class ParkourSpecialistTrait extends AbstractTraitDefinition {
    public static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("the_protocol", "parkour_speed_bonus");

    public ParkourSpecialistTrait() {
        super("parkour_specialist", Set.of());
    }

    @Override
    public void applyPersistentEffects(ServerPlayer player, PlayerTraitData data) {
        TraitService.applyParkourSpeedBonus(player);
    }

    @Override
    public void onGranted(ServerPlayer player, PlayerTraitData data) {
        player.displayClientMessage(Component.translatable("message.the_protocol.trait.parkour_ready").withStyle(ChatFormatting.AQUA), true);
    }

    public void handleFall(ServerPlayer player, LivingFallEvent event, PlayerTraitData data) {
        float distance = event.getDistance();
        if (distance < Config.parkourRollMinDistance()) {
            event.setDamageMultiplier((float) (event.getDamageMultiplier() * Config.parkourFallDamageMultiplier()));
            return;
        }

        long now = player.level().getGameTime();
        if (player.isCrouching() && now - data.lastParkourRollTick() >= Config.parkourRollCooldownTicks()) {
            event.setDamageMultiplier((float) (event.getDamageMultiplier() * Config.parkourRollDamageMultiplier()));
            TraitService.setData(player, data.withLastParkourRollTick(now));
            player.displayClientMessage(Component.translatable("message.the_protocol.trait.parkour_roll").withStyle(ChatFormatting.GREEN), true);
            return;
        }

        event.setDamageMultiplier((float) (event.getDamageMultiplier() * Config.parkourFallDamageMultiplier()));
    }
}
