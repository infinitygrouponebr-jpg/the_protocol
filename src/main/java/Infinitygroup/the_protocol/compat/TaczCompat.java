package Infinitygroup.the_protocol.compat;

import net.neoforged.fml.ModList;

public final class TaczCompat {
    public static final String MOD_ID = "tacz";

    private TaczCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static String describeStatus() {
        return isLoaded() ? "loaded" : "not loaded";
    }
}
