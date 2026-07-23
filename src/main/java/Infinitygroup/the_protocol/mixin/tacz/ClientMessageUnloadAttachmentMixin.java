package Infinitygroup.the_protocol.mixin.tacz;

import Infinitygroup.the_protocol.compat.TaczCompat;
import com.tacz.guns.network.message.ClientMessageUnloadAttachment;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents the TaCZ server packet from removing an installed attachment. */
@Mixin(ClientMessageUnloadAttachment.class)
public final class ClientMessageUnloadAttachmentMixin {
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void theProtocol$guardAttachmentRemoval(ClientMessageUnloadAttachment message, IPayloadContext context,
                                                           CallbackInfo callback) {
        if (context.player() instanceof ServerPlayer player && !TaczCompat.canModifyTaczWeapons(player)) {
            context.enqueueWork(() -> TaczCompat.notifyTaczModificationDenied(player));
            callback.cancel();
        }
    }
}
