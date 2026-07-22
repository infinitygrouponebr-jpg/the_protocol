package Infinitygroup.the_protocol.network;

import Infinitygroup.the_protocol.The_protocol;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ParkourRollRequestPayload(double dirX, double dirZ) implements CustomPacketPayload {
    public static final Type<ParkourRollRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(The_protocol.MODID, "parkour_roll_request")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ParkourRollRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, ParkourRollRequestPayload::dirX,
            ByteBufCodecs.DOUBLE, ParkourRollRequestPayload::dirZ,
            ParkourRollRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
