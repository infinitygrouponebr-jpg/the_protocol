package Infinitygroup.the_protocol.trait;

import Infinitygroup.the_protocol.Config;
import Infinitygroup.the_protocol.data.PlayerTraitData;
import Infinitygroup.the_protocol.network.TheProtocolNetworking;
import Infinitygroup.the_protocol.trait.impl.ParkourSpecialistTrait;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public final class ParkourRollHandler {
    public static final ResourceLocation PARKOUR_ROLL_KNOCKBACK_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("the_protocol", "parkour_roll_knockback_bonus");
    private static final long NOT_USED_TICK = -1L;

    private ParkourRollHandler() {
    }

    public static PlayerTraitData normalize(PlayerTraitData data) {
        if (data.lastParkourRollTick() < 0L && data.rollStartTick() < 0L && !data.rolling()) {
            return data.withLastParkourRollTick(NOT_USED_TICK).withoutParkourRollState();
        }

        PlayerTraitData normalized = data;
        if (normalized.lastParkourRollTick() < 0L) {
            normalized = normalized.withLastParkourRollTick(NOT_USED_TICK);
        }
        if (!normalized.rolling() && normalized.rollStartTick() < 0L) {
            normalized = normalized.withParkourRollState(false, NOT_USED_TICK, 0.0D, 0.0D, 0, 0);
        }
        return normalized;
    }

    public static PlayerTraitData resetForNewTrait(PlayerTraitData data) {
        return normalize(data)
                .withLastParkourRollTick(NOT_USED_TICK)
                .withoutParkourRollState()
                .withParkourStamina(Config.parkourMaxStamina());
    }

    public static boolean isParkourRollActive(PlayerTraitData data) {
        return data.rolling() && data.rollStartTick() >= 0L;
    }

    public static boolean wasRecentRoll(PlayerTraitData data, long now) {
        if (data.rollStartTick() < 0L) {
            return false;
        }

        int window = data.rollInvulnTicks() > 0 ? data.rollInvulnTicks() : Config.parkourRollFallWindowTicks();
        return now - data.rollStartTick() <= window;
    }

    public static boolean tryStartRoll(ServerPlayer player, double inputDirX, double inputDirZ) {
        if (player == null || !Config.traitsEnabled() || !TraitService.currentTrait(player).map(trait -> trait instanceof ParkourSpecialistTrait).orElse(false)) {
            return false;
        }

        PlayerTraitData data = normalize(TraitService.getData(player));
        if (data.rolling()) {
            return false;
        }

        if (!player.onGround() || player.isPassenger() || player.isFallFlying() || player.isSwimming() || player.isInWaterOrBubble() || player.isInLava()) {
            player.displayClientMessage(Component.translatable("message.the_protocol.trait.parkour_roll_ground").withStyle(ChatFormatting.RED), true);
            return false;
        }

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

        Vec3 direction = resolveDirection(player, inputDirX, inputDirZ);
        PlayerTraitData updated = data.withLastParkourRollTick(now)
                .withParkourStamina(Math.max(0.0D, data.parkourStamina() - cost))
                .withParkourRollState(true, now, direction.x, direction.z, Config.parkourRollDurationTicks(), Config.parkourRollFallWindowTicks());
        TraitService.setData(player, updated);
        applyKnockbackResistance(player, true);
        player.resetFallDistance();
        TheProtocolNetworking.syncParkourRollStart(player);
        player.displayClientMessage(Component.translatable("message.the_protocol.trait.parkour_rolled").withStyle(ChatFormatting.GREEN), true);
        return true;
    }

    public static void tickRoll(ServerPlayer player) {
        if (player == null || !Config.traitsEnabled()) {
            return;
        }

        PlayerTraitData data = normalize(TraitService.getData(player));
        if (!isParkourRollActive(data)) {
            return;
        }

        if (!TraitService.currentTrait(player).map(trait -> trait instanceof ParkourSpecialistTrait).orElse(false)) {
            stopRoll(player, true);
            return;
        }

        long now = player.level().getGameTime();
        int duration = data.rollDurationTicks() > 0 ? data.rollDurationTicks() : Config.parkourRollDurationTicks();
        long elapsed = now - data.rollStartTick();
        if (elapsed >= duration) {
            stopRoll(player, false);
            return;
        }

        double progress = Math.max(0.0D, Math.min(1.0D, (double) elapsed / (double) duration));
        double curve = 1.0D - progress;
        double speed = Config.parkourRollEndSpeed() + ((Config.parkourRollInitialSpeed() - Config.parkourRollEndSpeed()) * (curve * curve));
        Vec3 rollDirection = new Vec3(data.rollDirX(), 0.0D, data.rollDirZ());
        if (rollDirection.lengthSqr() < 1.0E-4D) {
            rollDirection = horizontalLookDirection(player);
        } else {
            rollDirection = rollDirection.normalize();
        }

        Vec3 current = player.getDeltaMovement();
        Vec3 control = new Vec3(current.x, 0.0D, current.z).scale(Config.parkourRollControlMultiplier());
        Vec3 desired = rollDirection.scale(speed).add(control);
        double y = player.onGround() && current.y < 0.0D ? 0.0D : current.y;
        player.setDeltaMovement(desired.x, y, desired.z);
        player.resetFallDistance();
    }

    public static void stopRoll(ServerPlayer player, boolean interrupted) {
        if (player == null) {
            return;
        }

        PlayerTraitData data = normalize(TraitService.getData(player));
        if (!data.rolling()) {
            return;
        }

        TraitService.setData(player, data.withParkourRollState(false, data.rollStartTick(), 0.0D, 0.0D, 0, 0));
        applyKnockbackResistance(player, false);
        TheProtocolNetworking.syncParkourRollStop(player);
        if (interrupted) {
            player.resetFallDistance();
        }
    }

    private static void applyKnockbackResistance(ServerPlayer player, boolean active) {
        AttributeInstance resistance = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (resistance == null) {
            return;
        }

        if (resistance.getModifier(PARKOUR_ROLL_KNOCKBACK_MODIFIER_ID) != null) {
            resistance.removeModifier(PARKOUR_ROLL_KNOCKBACK_MODIFIER_ID);
        }

        double bonus = Config.parkourRollKnockbackResistanceBonus();
        if (active && bonus > 0.0D) {
            resistance.addTransientModifier(new AttributeModifier(PARKOUR_ROLL_KNOCKBACK_MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static Vec3 resolveDirection(ServerPlayer player, double inputDirX, double inputDirZ) {
        Vec3 input = new Vec3(inputDirX, 0.0D, inputDirZ);
        if (input.lengthSqr() >= 1.0E-4D) {
            return input.normalize();
        }

        return horizontalLookDirection(player);
    }

    private static Vec3 horizontalLookDirection(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        return horizontal.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : horizontal.normalize();
    }
}
