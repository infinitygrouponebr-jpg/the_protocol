package Infinitygroup.the_protocol.profession;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Persisted player data. Cooldowns are intentionally reserved for future perks. */
public record ProfessionData(ProfessionType profession, int level, int experience, int perkPoints) {
    public static final Codec<ProfessionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("profession", ProfessionType.NONE.name()).forGetter(data -> data.profession().name()),
            Codec.INT.optionalFieldOf("level", 0).forGetter(ProfessionData::level),
            Codec.INT.optionalFieldOf("experience", 0).forGetter(ProfessionData::experience),
            Codec.INT.optionalFieldOf("perkPoints", 0).forGetter(ProfessionData::perkPoints)
    ).apply(instance, (profession, level, experience, perkPoints) -> new ProfessionData(ProfessionType.fromName(profession), level, experience, perkPoints)));

    public ProfessionData {
        profession = profession == null ? ProfessionType.NONE : profession;
        level = Math.max(0, level);
        experience = Math.max(0, experience);
        perkPoints = Math.max(0, perkPoints);
    }

    public static ProfessionData empty() {
        return new ProfessionData(ProfessionType.NONE, 0, 0, 0);
    }

    public Profession asProfession() {
        return new Profession(profession, level, experience, perkPoints);
    }
}
