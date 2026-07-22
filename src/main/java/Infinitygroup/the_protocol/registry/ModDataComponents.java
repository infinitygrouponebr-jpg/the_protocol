package Infinitygroup.the_protocol.registry;

import Infinitygroup.the_protocol.profession.PlayerProfessionProvider;
import net.neoforged.bus.api.IEventBus;

/** Registers persisted NeoForge attachments used by this mod. */
public final class ModDataComponents {
    private ModDataComponents() {
    }

    public static void register(IEventBus eventBus) {
        PlayerProfessionProvider.register(eventBus);
    }
}
