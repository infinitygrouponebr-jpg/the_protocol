package Infinitygroup.the_protocol.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record PlayerTraitData(String traitId, boolean initialGranted, boolean rerollUsed, long lastParkourRollTick) {
    public static final Codec<PlayerTraitData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("traitId", "").forGetter(PlayerTraitData::traitId),
            Codec.BOOL.optionalFieldOf("initialGranted", false).forGetter(PlayerTraitData::initialGranted),
            Codec.BOOL.optionalFieldOf("rerollUsed", false).forGetter(PlayerTraitData::rerollUsed),
            Codec.LONG.optionalFieldOf("lastParkourRollTick", Long.MIN_VALUE).forGetter(PlayerTraitData::lastParkourRollTick)
    ).apply(instance, PlayerTraitData::new));

    public static PlayerTraitData empty() {
        return new PlayerTraitData("", false, false, Long.MIN_VALUE);
    }

    public boolean hasTrait() {
        return traitId != null && !traitId.isBlank();
    }

    public ResourceLocation traitLocation() {
        return hasTrait() ? ResourceLocation.tryParse(traitId) : null;
    }

    public PlayerTraitData withTrait(ResourceLocation traitLocation) {
        return new PlayerTraitData(traitLocation.toString(), initialGranted, rerollUsed, lastParkourRollTick);
    }

    public PlayerTraitData withInitialGranted(boolean value) {
        return new PlayerTraitData(traitId, value, rerollUsed, lastParkourRollTick);
    }

    public PlayerTraitData withRerollUsed(boolean value) {
        return new PlayerTraitData(traitId, initialGranted, value, lastParkourRollTick);
    }

    public PlayerTraitData withLastParkourRollTick(long value) {
        return new PlayerTraitData(traitId, initialGranted, rerollUsed, value);
    }
}
