package Infinitygroup.the_protocol.network;

import Infinitygroup.the_protocol.The_protocol;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ParkourRollSyncPayload(int entityId, boolean starting) implements CustomPacketPayload {
    public static final Type<ParkourRollSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(The_protocol.MODID, "parkour_roll_sync")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ParkourRollSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ParkourRollSyncPayload::entityId,
            ByteBufCodecs.BOOL, ParkourRollSyncPayload::starting,
            ParkourRollSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
