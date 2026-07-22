package Infinitygroup.the_protocol.client;

import Infinitygroup.the_protocol.The_protocol;
import Infinitygroup.the_protocol.network.ParkourRollRequestPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = The_protocol.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public final class ParkourClientInput {
    private ParkourClientInput() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        while (ParkourClientEvents.PARKOUR_ROLL.get().consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            Vec3 direction = resolveRollDirection(minecraft);
            PacketDistributor.sendToServer(new ParkourRollRequestPayload(direction.x, direction.z));
        }
    }

    private static Vec3 resolveRollDirection(Minecraft minecraft) {
        if (minecraft.player == null) {
            return new Vec3(0.0D, 0.0D, 1.0D);
        }

        float forward = 0.0F;
        if (minecraft.options.keyUp.isDown()) {
            forward += 1.0F;
        }
        if (minecraft.options.keyDown.isDown()) {
            forward -= 1.0F;
        }

        float strafe = 0.0F;
        if (minecraft.options.keyLeft.isDown()) {
            strafe += 1.0F;
        }
        if (minecraft.options.keyRight.isDown()) {
            strafe -= 1.0F;
        }

        Vec3 look = minecraft.player.getLookAngle();
        Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
        if (horizontalLook.lengthSqr() < 1.0E-4D) {
            horizontalLook = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            horizontalLook = horizontalLook.normalize();
        }

        if (forward == 0.0F && strafe == 0.0F) {
            return horizontalLook;
        }

        Vec3 left = new Vec3(-horizontalLook.z, 0.0D, horizontalLook.x);
        return horizontalLook.scale(forward).add(left.scale(strafe)).normalize();
    }
}
