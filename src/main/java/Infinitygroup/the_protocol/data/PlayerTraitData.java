package Infinitygroup.the_protocol.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record PlayerTraitData(String traitId, boolean initialGranted, boolean rerollUsed, long lastParkourRollTick, double parkourStamina) {
    public static final Codec<PlayerTraitData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("traitId", "").forGetter(PlayerTraitData::traitId),
            Codec.BOOL.optionalFieldOf("initialGranted", false).forGetter(PlayerTraitData::initialGranted),
            Codec.BOOL.optionalFieldOf("rerollUsed", false).forGetter(PlayerTraitData::rerollUsed),
            Codec.LONG.optionalFieldOf("lastParkourRollTick", -1L).forGetter(PlayerTraitData::lastParkourRollTick),
            Codec.DOUBLE.optionalFieldOf("parkourStamina", 100.0D).forGetter(PlayerTraitData::parkourStamina)
    ).apply(instance, PlayerTraitData::new));

    public static PlayerTraitData empty() {
        return new PlayerTraitData("", false, false, -1L, 100.0D);
    }

    public boolean hasTrait() {
        return traitId != null && !traitId.isBlank();
    }

    public ResourceLocation traitLocation() {
        return hasTrait() ? ResourceLocation.tryParse(traitId) : null;
    }

    public PlayerTraitData withTrait(ResourceLocation traitLocation) {
        return new PlayerTraitData(traitLocation.toString(), initialGranted, rerollUsed, lastParkourRollTick, parkourStamina);
    }

    public PlayerTraitData withInitialGranted(boolean value) {
        return new PlayerTraitData(traitId, value, rerollUsed, lastParkourRollTick, parkourStamina);
    }

    public PlayerTraitData withRerollUsed(boolean value) {
        return new PlayerTraitData(traitId, initialGranted, value, lastParkourRollTick, parkourStamina);
    }

    public PlayerTraitData withLastParkourRollTick(long value) {
        return new PlayerTraitData(traitId, initialGranted, rerollUsed, value, parkourStamina);
    }

    public PlayerTraitData withParkourStamina(double value) {
        return new PlayerTraitData(traitId, initialGranted, rerollUsed, lastParkourRollTick, value);
    }
}
