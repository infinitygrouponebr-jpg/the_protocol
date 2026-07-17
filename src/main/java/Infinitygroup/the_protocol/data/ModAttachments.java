package Infinitygroup.the_protocol.data;

import Infinitygroup.the_protocol.The_protocol;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, The_protocol.MODID);

    public static final Supplier<AttachmentType<PlayerTraitData>> PLAYER_TRAIT_DATA = ATTACHMENT_TYPES.register("player_trait_data", () -> AttachmentType.builder(PlayerTraitData::empty)
            .serialize(PlayerTraitData.CODEC)
            .copyOnDeath()
            .build());

    private ModAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
