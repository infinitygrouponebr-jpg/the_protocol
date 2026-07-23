package Infinitygroup.the_protocol.network;

import Infinitygroup.the_protocol.SurvivalProfessionsMod;
import Infinitygroup.the_protocol.compat.CombatRollCompat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client signal sent only when Combat Roll's profession marker prevents a roll-key press. */
public record RollDeniedAttemptPayload() implements CustomPacketPayload {
    public static final Type<RollDeniedAttemptPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            SurvivalProfessionsMod.MOD_ID, "roll_denied_attempt"));
    public static final RollDeniedAttemptPayload INSTANCE = new RollDeniedAttemptPayload();
    public static final StreamCodec<RegistryFriendlyByteBuf, RollDeniedAttemptPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<RollDeniedAttemptPayload> type() {
        return TYPE;
    }

    public static void handle(RollDeniedAttemptPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && CombatRollCompat.shouldRestrictRoll() && !CombatRollCompat.canRoll(player)) {
                CombatRollCompat.notifyRollDenied(player);
            }
        });
    }
}
