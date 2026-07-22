package Infinitygroup.the_protocol.util;

import Infinitygroup.the_protocol.SurvivalProfessionsMod;

public final class ProfessionDebug {
    private ProfessionDebug() {
    }

    public static void log(String message) {
        SurvivalProfessionsMod.LOGGER.debug("[Profession Debug] {}", message);
    }
}
