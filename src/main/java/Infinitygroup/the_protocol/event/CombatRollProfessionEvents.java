package Infinitygroup.the_protocol.event;

import Infinitygroup.the_protocol.compat.CombatRollCompat;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Reapplies the transient Combat Roll blocker after player lifecycle transitions. */
public final class CombatRollProfessionEvents {
    private CombatRollProfessionEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        refreshNextServerTask(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        refreshNextServerTask(event.getEntity());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        refreshNextServerTask(event.getEntity());
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        refreshNextServerTask(event.getEntity());
    }

    private static void refreshNextServerTask(net.minecraft.world.entity.player.Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            return;
        }
        serverPlayer.getServer().execute(() -> CombatRollCompat.refreshRollPermission(serverPlayer));
    }
}
