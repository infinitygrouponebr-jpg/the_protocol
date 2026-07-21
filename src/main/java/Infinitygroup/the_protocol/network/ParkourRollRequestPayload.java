package Infinitygroup.the_protocol.network;

import Infinitygroup.the_protocol.The_protocol;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ParkourRollRequestPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ParkourRollRequestPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(The_protocol.MODID, "parkour_roll_request")
    );

    public static final StreamCodec<ByteBuf, ParkourRollRequestPayload> STREAM_CODEC = StreamCodec.unit(new ParkourRollRequestPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
