package Infinitygroup.the_protocol.network;

import Infinitygroup.the_protocol.The_protocol;
import Infinitygroup.the_protocol.client.ParkourRollClientSync;
import Infinitygroup.the_protocol.trait.TraitService;
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
        registrar.playToClient(ParkourRollSyncPayload.TYPE, ParkourRollSyncPayload.STREAM_CODEC, TheProtocolNetworking::handleParkourRollSync);
    }

    private static void handleParkourRollRequest(ParkourRollRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                TraitService.tryTriggerParkourRoll(serverPlayer, payload.dirX(), payload.dirZ());
            }
        });
    }

    private static void handleParkourRollSync(ParkourRollSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ParkourRollClientSync.handle(payload));
    }

    public static void syncParkourRollStart(net.minecraft.server.level.ServerPlayer player) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new ParkourRollSyncPayload(player.getId(), true));
    }

    public static void syncParkourRollStop(net.minecraft.server.level.ServerPlayer player) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new ParkourRollSyncPayload(player.getId(), false));
    }
}
