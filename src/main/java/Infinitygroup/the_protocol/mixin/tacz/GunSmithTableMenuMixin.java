package Infinitygroup.the_protocol.mixin.tacz;

import Infinitygroup.the_protocol.compat.TaczCompat;
import com.tacz.guns.inventory.GunSmithTableMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Guards the server-side TaCZ gunsmith confirmation method, including packet-originated crafts. */
@Mixin(GunSmithTableMenu.class)
public final class GunSmithTableMenuMixin {
    @Inject(method = "doCraft", at = @At("HEAD"), cancellable = true)
    private void theProtocol$guardGunSmithCraft(ResourceLocation recipeId, Player player, CallbackInfo callback) {
        if (player instanceof ServerPlayer serverPlayer && !TaczCompat.canModifyTaczWeapons(serverPlayer)) {
            TaczCompat.notifyTaczModificationDenied(serverPlayer);
            callback.cancel();
        }
    }
}
