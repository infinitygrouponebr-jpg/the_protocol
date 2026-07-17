package Infinitygroup.the_protocol.trait.impl;

import Infinitygroup.the_protocol.data.PlayerTraitData;
import Infinitygroup.the_protocol.trait.AbstractTraitDefinition;
import Infinitygroup.the_protocol.trait.TraitService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public final class MechanicTrait extends AbstractTraitDefinition {
    public MechanicTrait() {
        super("mechanic", Set.of());
    }

    @Override
    public void onGranted(ServerPlayer player, PlayerTraitData data) {
        TraitService.giveMechanicStarterKit(player);
        if (!Infinitygroup.the_protocol.Config.mechanicStarterKitItems().isEmpty()) {
            player.displayClientMessage(Component.translatable("message.the_protocol.trait.mechanic_kit").withStyle(ChatFormatting.YELLOW), true);
        }
    }
}
