package Infinitygroup.the_protocol.trait;

import Infinitygroup.the_protocol.trait.impl.GunSpecialistTrait;
import Infinitygroup.the_protocol.trait.impl.MechanicTrait;
import Infinitygroup.the_protocol.trait.impl.ParkourSpecialistTrait;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TraitRegistry {
    private static final Map<ResourceLocation, TraitDefinition> BY_ID = new LinkedHashMap<>();
    private static final List<TraitDefinition> ALL = new ArrayList<>();
    private static boolean bootstrapped;

    private TraitRegistry() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }

        register(new GunSpecialistTrait());
        register(new ParkourSpecialistTrait());
        register(new MechanicTrait());
        bootstrapped = true;
    }

    public static synchronized TraitDefinition register(TraitDefinition trait) {
        TraitDefinition previous = BY_ID.putIfAbsent(trait.id(), trait);
        if (previous != null) {
            return previous;
        }

        ALL.add(trait);
        return trait;
    }

    public static List<TraitDefinition> allTraits() {
        return Collections.unmodifiableList(ALL);
    }

    public static Optional<TraitDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static Optional<TraitDefinition> get(String id) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        return parsed == null ? Optional.empty() : get(parsed);
    }
}
