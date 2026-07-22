package Infinitygroup.the_protocol.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Short-lived server-only attribution for delayed TaCZ kills. */
public final class TaczDamageTracker {
    private static final long MAX_AGE_TICKS = 100;
    private static final Map<UUID, Hit> HITS = new HashMap<>();

    private TaczDamageTracker() {
    }

    public static void record(LivingEntity target, ServerPlayer attacker, TaczWeaponCategory category) {
        long now = target.level().getGameTime();
        HITS.put(target.getUUID(), new Hit(attacker.getUUID(), now, TaczCompat.isShooter(attacker), category));
        if ((now & 31L) == 0L) {
            prune(now);
        }
    }

    public static Hit takeRecent(LivingEntity target) {
        Hit hit = HITS.remove(target.getUUID());
        return hit != null && target.level().getGameTime() - hit.gameTime() <= MAX_AGE_TICKS ? hit : null;
    }

    public static void prune(long now) {
        Iterator<Hit> iterator = HITS.values().iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().gameTime() > MAX_AGE_TICKS) {
                iterator.remove();
            }
        }
    }

    public record Hit(UUID attackerId, long gameTime, boolean shooter, TaczWeaponCategory category) {
    }
}
