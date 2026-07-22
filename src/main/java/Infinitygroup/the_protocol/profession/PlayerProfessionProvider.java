package Infinitygroup.the_protocol.profession;

import Infinitygroup.the_protocol.SurvivalProfessionsMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class PlayerProfessionProvider {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SurvivalProfessionsMod.MOD_ID);
    public static final Supplier<AttachmentType<ProfessionData>> PROFESSION_DATA = ATTACHMENTS.register("profession_data",
            () -> AttachmentType.builder(ProfessionData::empty).serialize(ProfessionData.CODEC).copyOnDeath().build());

    private PlayerProfessionProvider() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENTS.register(eventBus);
    }
}
