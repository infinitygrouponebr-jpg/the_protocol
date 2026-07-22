package Infinitygroup.the_protocol.client;

import Infinitygroup.the_protocol.Config;
import Infinitygroup.the_protocol.The_protocol;
import Infinitygroup.the_protocol.network.ParkourRollSyncPayload;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.enums.PlayState;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = The_protocol.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ParkourRollClient {
    public static final ResourceLocation PARKOUR_ROLL_LAYER_ID = ResourceLocation.fromNamespaceAndPath(The_protocol.MODID, "parkour_roll");
    public static final ResourceLocation PARKOUR_ROLL_ANIMATION_ID = ResourceLocation.fromNamespaceAndPath(The_protocol.MODID, "parkour_roll");

    private ParkourRollClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                PARKOUR_ROLL_LAYER_ID,
                1700,
                player -> new PlayerAnimationController(player, (controller, state, animSetter) -> PlayState.CONTINUE)
        ));
    }

    public static void handleSync(ParkourRollSyncPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        if (!(minecraft.level.getEntity(payload.entityId()) instanceof AbstractClientPlayer player)) {
            return;
        }

        PlayerAnimationController controller = getController(player);
        if (controller == null) {
            return;
        }

        if (payload.starting()) {
            controller.forceAnimationReset();
            controller.triggerAnimation(PARKOUR_ROLL_ANIMATION_ID);
        } else {
            int fadeTicks = Math.max(0, Config.parkourRollAnimationFadeTicks());
            if (fadeTicks <= 0) {
                controller.stopTriggeredAnimation();
            } else {
                controller.replaceAnimationWithFade(
                        AbstractFadeModifier.standardFadeOut(fadeTicks, EasingType.EASE_IN_OUT_SINE),
                        (ResourceLocation) null,
                        false
                );
            }
        }
    }

    private static PlayerAnimationController getController(AbstractClientPlayer player) {
        try {
            return (PlayerAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(player, PARKOUR_ROLL_LAYER_ID);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
