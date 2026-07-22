package Infinitygroup.the_protocol.event;

import Infinitygroup.the_protocol.config.CommonConfig;
import Infinitygroup.the_protocol.profession.ProfessionManager;
import Infinitygroup.the_protocol.profession.ProfessionType;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Initializes a player's saved profession without granting gameplay effects. */
public final class PlayerProfessionEvents {
    private PlayerProfessionEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !CommonConfig.ENABLE_PROFESSION_SYSTEM.get()) {
            return;
        }
        if (ProfessionManager.get(player).profession() == ProfessionType.NONE) {
            ProfessionType startingProfession = ProfessionType.fromName(CommonConfig.STARTING_PROFESSION.get());
            if (startingProfession != ProfessionType.NONE) {
                ProfessionManager.setProfession(player, startingProfession);
            }
        }
    }
}
