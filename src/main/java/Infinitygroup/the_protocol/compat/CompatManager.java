package Infinitygroup.the_protocol.compat;

import net.neoforged.fml.ModList;

/** Isolated optional-mod checks. Do not import external mod classes here. */
public final class CompatManager {
    private CompatManager() {
    }

    public static boolean isTaczLoaded() {
        return ModList.get().isLoaded("tacz");
    }

    public static boolean isVehicleModLoaded() {
        return ModList.get().isLoaded("immersivevehicles") || ModList.get().isLoaded("ultimatecar");
    }

    public static boolean isMicroTechLoaded() {
        return ModList.get().isLoaded("microtech");
    }
}
