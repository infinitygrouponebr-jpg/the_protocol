package Infinitygroup.the_protocol.client;

import Infinitygroup.the_protocol.network.RollDeniedAttemptPayload;
import Infinitygroup.the_protocol.SurvivalProfessionsMod;
import net.combat_roll.api.CombatRoll;
import net.combat_roll.client.Keybindings;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Client-only bridge for feedback when Combat Roll rejects our profession blocker locally. */
@EventBusSubscriber(modid = SurvivalProfessionsMod.MOD_ID, value = Dist.CLIENT)
public final class CombatRollInputFeedback {
    private static boolean rollKeyWasDown;

    private CombatRollInputFeedback() {
    }

    @SubscribeEvent
    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean rollKeyDown = Keybindings.roll.isDown();

        if (rollKeyDown && !rollKeyWasDown && minecraft.player != null && minecraft.screen == null
                && !minecraft.isPaused()
                && minecraft.player.getAttributeValue(CombatRoll.Attributes.COUNT.entry) < 0.0D) {
            PacketDistributor.sendToServer(RollDeniedAttemptPayload.INSTANCE);
        }
        rollKeyWasDown = rollKeyDown;
    }
}
