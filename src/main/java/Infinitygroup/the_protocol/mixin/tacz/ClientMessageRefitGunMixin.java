package Infinitygroup.the_protocol.mixin.tacz;

import Infinitygroup.the_protocol.compat.TaczCompat;
import com.tacz.guns.network.message.ClientMessageRefitGun;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents the TaCZ server packet from installing an attachment before it mutates the gun. */
@Mixin(ClientMessageRefitGun.class)
public final class ClientMessageRefitGunMixin {
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void theProtocol$guardAttachmentInstall(ClientMessageRefitGun message, IPayloadContext context,
                                                           CallbackInfo callback) {
        if (context.player() instanceof ServerPlayer player && !TaczCompat.canModifyTaczWeapons(player)) {
            context.enqueueWork(() -> TaczCompat.notifyTaczModificationDenied(player));
            callback.cancel();
        }
    }
}
