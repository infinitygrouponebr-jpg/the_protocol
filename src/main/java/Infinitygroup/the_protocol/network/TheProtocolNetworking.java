package Infinitygroup.the_protocol.network;

import Infinitygroup.the_protocol.The_protocol;
import Infinitygroup.the_protocol.trait.TraitService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = The_protocol.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class TheProtocolNetworking {
    private TheProtocolNetworking() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ParkourRollRequestPayload.TYPE, ParkourRollRequestPayload.STREAM_CODEC, TheProtocolNetworking::handleParkourRollRequest);
    }

    private static void handleParkourRollRequest(ParkourRollRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                TraitService.tryTriggerParkourRoll(serverPlayer);
            }
        });
    }
}
