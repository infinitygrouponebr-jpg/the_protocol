package Infinitygroup.the_protocol.client;

import Infinitygroup.the_protocol.The_protocol;
import Infinitygroup.the_protocol.network.ParkourRollRequestPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = The_protocol.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public final class ParkourClientInput {
    private ParkourClientInput() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        while (ParkourClientEvents.PARKOUR_ROLL.get().consumeClick()) {
            PacketDistributor.sendToServer(new ParkourRollRequestPayload());
        }
    }
}
