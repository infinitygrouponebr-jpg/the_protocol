package Infinitygroup.the_protocol.compat;

import Infinitygroup.the_protocol.Config;

import java.util.List;

public final class CompatManager {
    private CompatManager() {
    }

    public static boolean isTaczLoaded() {
        return TaczCompat.isLoaded();
    }

    public static boolean isVehicleCompatLoaded() {
        return VehicleCompat.isLoaded();
    }

    public static List<String> describeVehicleCompatTargets() {
        return Config.vehicleCompatModIds();
    }
}
