package Infinitygroup.the_protocol.compat;

import Infinitygroup.the_protocol.Config;
import net.neoforged.fml.ModList;

import java.util.List;

public final class VehicleCompat {
    private VehicleCompat() {
    }

    public static boolean isLoaded() {
        List<String> modIds = Config.vehicleCompatModIds();
        return !modIds.isEmpty() && modIds.stream().anyMatch(modId -> ModList.get().isLoaded(modId));
    }

    public static List<String> loadedModIds() {
        return Config.vehicleCompatModIds().stream().filter(modId -> ModList.get().isLoaded(modId)).toList();
    }

    public static String describeStatus() {
        return loadedModIds().toString();
    }
}
