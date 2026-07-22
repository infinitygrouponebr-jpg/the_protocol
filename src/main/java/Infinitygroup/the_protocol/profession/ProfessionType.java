package Infinitygroup.the_protocol.profession;

import java.util.Locale;

public enum ProfessionType {
    NONE,
    SHOOTER,
    PARKOUR,
    MECHANIC,
    MEDIC,
    ENGINEER,
    DRIVER,
    SURVIVOR;

    public static ProfessionType fromName(String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return NONE;
        }
    }
}
