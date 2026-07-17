package Infinitygroup.the_protocol.trait.impl;

import Infinitygroup.the_protocol.Config;
import Infinitygroup.the_protocol.The_protocol;
import Infinitygroup.the_protocol.trait.AbstractTraitDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;

import java.util.Set;

public final class GunSpecialistTrait extends AbstractTraitDefinition {
    public GunSpecialistTrait() {
        super("gun_specialist", Set.of());
    }

    @Override
    public void onArrowLoose(ServerPlayer player, ArrowLooseEvent event, Infinitygroup.the_protocol.data.PlayerTraitData data) {
        if (!Config.traitsEnabled() || event.getBow().is(Items.CROSSBOW)) {
            return;
        }

        if (event.getBow().is(Items.BOW)) {
            event.setCharge(Math.max(0, event.getCharge() - Config.gunBowChargeReductionTicks()));
        }
    }
}
