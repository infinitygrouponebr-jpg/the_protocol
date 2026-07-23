package Infinitygroup.the_protocol.event;

import Infinitygroup.the_protocol.compat.TaczCompat;
import com.tacz.guns.api.event.common.GunMeleeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;

/** Cancels TaCZ's server-side weapon-bash event before animation, cooldown, or damage are scheduled. */
public final class TaczMeleeEvents {
    private TaczMeleeEvents() {
    }

    @SubscribeEvent
    public static void onGunMelee(GunMeleeEvent event) {
        if (event.getLogicalSide() != LogicalSide.SERVER || !(event.getShooter() instanceof ServerPlayer player)
                || TaczCompat.canUseTaczMelee(player)) {
            return;
        }
        event.setCanceled(true);
        TaczCompat.notifyMeleeDenied(player);
    }
}
