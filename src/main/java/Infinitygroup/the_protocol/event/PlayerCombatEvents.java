package Infinitygroup.the_protocol.event;

import Infinitygroup.the_protocol.compat.TaczCompat;
import Infinitygroup.the_protocol.config.CommonConfig;
import Infinitygroup.the_protocol.profession.ProfessionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Server-side Phase 1 TaCZ profession effects. */
public final class PlayerCombatEvents {
    private PlayerCombatEvents() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || !(event.getEntity() instanceof LivingEntity target)
                || target == player || !TaczCompat.isEnabled() || !TaczCompat.isHoldingTaczWeapon(player)) {
            return;
        }

        boolean shooter = TaczCompat.isShooter(player);
        if (!shooter && !CommonConfig.TACZ_ALLOW_NON_SHOOTERS.get()) {
            event.setNewDamage(0.0F);
            return;
        }

        double multiplier = shooter
                ? (CommonConfig.TACZ_ENABLE_SHOOTER_BONUS.get() ? CommonConfig.TACZ_SHOOTER_DAMAGE_MULTIPLIER.get() : 1.0D)
                : (CommonConfig.TACZ_ENABLE_NON_SHOOTER_PENALTY.get() ? CommonConfig.TACZ_NON_SHOOTER_DAMAGE_MULTIPLIER.get() : 1.0D);
        float adjustedDamage = (float) (event.getNewDamage() * multiplier);
        event.setNewDamage(adjustedDamage);

        if (adjustedDamage > 0.0F && CommonConfig.ENABLE_PROFESSION_SYSTEM.get() && CommonConfig.TACZ_ENABLE_XP_GAIN.get()) {
            ProfessionManager.addExperience(player, shooter
                    ? CommonConfig.TACZ_SHOOTER_XP_ON_HIT.get()
                    : CommonConfig.TACZ_NON_SHOOTER_XP_ON_HIT.get());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || !TaczCompat.isEnabled()
                || !TaczCompat.isHoldingTaczWeapon(player) || !CommonConfig.ENABLE_PROFESSION_SYSTEM.get()
                || !CommonConfig.TACZ_ENABLE_XP_GAIN.get()) {
            return;
        }
        ProfessionManager.addExperience(player, TaczCompat.isShooter(player)
                ? CommonConfig.TACZ_SHOOTER_XP_ON_KILL.get()
                : CommonConfig.TACZ_NON_SHOOTER_XP_ON_KILL.get());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TaczCompat.refreshHeavyWeaponMovementPenalty(player);
        }
    }
}
