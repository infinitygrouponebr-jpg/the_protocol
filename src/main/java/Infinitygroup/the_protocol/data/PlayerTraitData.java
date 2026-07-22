package Infinitygroup.the_protocol.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record PlayerTraitData(
        String traitId,
        boolean initialGranted,
        boolean rerollUsed,
        long lastParkourRollTick,
        double parkourStamina,
        boolean rolling,
        long rollStartTick,
        double rollDirX,
        double rollDirZ,
        int rollDurationTicks,
        int rollInvulnTicks
) {
    public static final Codec<PlayerTraitData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("traitId", "").forGetter(PlayerTraitData::traitId),
            Codec.BOOL.optionalFieldOf("initialGranted", false).forGetter(PlayerTraitData::initialGranted),
            Codec.BOOL.optionalFieldOf("rerollUsed", false).forGetter(PlayerTraitData::rerollUsed),
            Codec.LONG.optionalFieldOf("lastParkourRollTick", -1L).forGetter(PlayerTraitData::lastParkourRollTick),
            Codec.DOUBLE.optionalFieldOf("parkourStamina", 100.0D).forGetter(PlayerTraitData::parkourStamina),
            Codec.BOOL.optionalFieldOf("rolling", false).forGetter(PlayerTraitData::rolling),
            Codec.LONG.optionalFieldOf("rollStartTick", -1L).forGetter(PlayerTraitData::rollStartTick),
            Codec.DOUBLE.optionalFieldOf("rollDirX", 0.0D).forGetter(PlayerTraitData::rollDirX),
            Codec.DOUBLE.optionalFieldOf("rollDirZ", 0.0D).forGetter(PlayerTraitData::rollDirZ),
            Codec.INT.optionalFieldOf("rollDurationTicks", 0).forGetter(PlayerTraitData::rollDurationTicks),
            Codec.INT.optionalFieldOf("rollInvulnTicks", 0).forGetter(PlayerTraitData::rollInvulnTicks)
    ).apply(instance, PlayerTraitData::new));

    public static PlayerTraitData empty() {
        return new PlayerTraitData("", false, false, -1L, 100.0D, false, -1L, 0.0D, 0.0D, 0, 0);
    }

    public boolean hasTrait() {
        return traitId != null && !traitId.isBlank();
    }

    public ResourceLocation traitLocation() {
        return hasTrait() ? ResourceLocation.tryParse(traitId) : null;
    }

    public PlayerTraitData withTrait(ResourceLocation traitLocation) {
        return new PlayerTraitData(traitLocation.toString(), initialGranted, rerollUsed, lastParkourRollTick, parkourStamina, rolling, rollStartTick, rollDirX, rollDirZ, rollDurationTicks, rollInvulnTicks);
    }

    public PlayerTraitData withInitialGranted(boolean value) {
        return new PlayerTraitData(traitId, value, rerollUsed, lastParkourRollTick, parkourStamina, rolling, rollStartTick, rollDirX, rollDirZ, rollDurationTicks, rollInvulnTicks);
    }

    public PlayerTraitData withRerollUsed(boolean value) {
        return new PlayerTraitData(traitId, initialGranted, value, lastParkourRollTick, parkourStamina, rolling, rollStartTick, rollDirX, rollDirZ, rollDurationTicks, rollInvulnTicks);
    }

    public PlayerTraitData withLastParkourRollTick(long value) {
        return new PlayerTraitData(traitId, initialGranted, rerollUsed, value, parkourStamina, rolling, rollStartTick, rollDirX, rollDirZ, rollDurationTicks, rollInvulnTicks);
    }

    public PlayerTraitData withParkourStamina(double value) {
        return new PlayerTraitData(traitId, initialGranted, rerollUsed, lastParkourRollTick, value, rolling, rollStartTick, rollDirX, rollDirZ, rollDurationTicks, rollInvulnTicks);
    }

    public PlayerTraitData withParkourRollState(boolean rolling, long rollStartTick, double rollDirX, double rollDirZ, int rollDurationTicks, int rollInvulnTicks) {
        return new PlayerTraitData(traitId, initialGranted, rerollUsed, lastParkourRollTick, parkourStamina, rolling, rollStartTick, rollDirX, rollDirZ, rollDurationTicks, rollInvulnTicks);
    }

    public PlayerTraitData withoutParkourRollState() {
        return withParkourRollState(false, -1L, 0.0D, 0.0D, 0, 0);
    }
}
