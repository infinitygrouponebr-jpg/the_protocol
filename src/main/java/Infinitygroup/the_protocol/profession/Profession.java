package Infinitygroup.the_protocol.profession;

public record Profession(ProfessionType type, int level, int experience, int perkPoints) {
    public Profession {
        type = type == null ? ProfessionType.NONE : type;
        level = Math.max(0, level);
        experience = Math.max(0, experience);
        perkPoints = Math.max(0, perkPoints);
    }

    public static Profession empty() {
        return new Profession(ProfessionType.NONE, 0, 0, 0);
    }
}
