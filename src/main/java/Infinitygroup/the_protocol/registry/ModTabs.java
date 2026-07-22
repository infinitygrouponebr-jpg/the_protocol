package Infinitygroup.the_protocol.registry;

import Infinitygroup.the_protocol.SurvivalProfessionsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SurvivalProfessionsMod.MOD_ID);
    public static final Supplier<CreativeModeTab> SURVIVAL_PROFESSIONS = TABS.register("survival_professions", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.the_protocol.survival_professions"))
            .icon(() -> new ItemStack(ModItems.PROFESSION_DEBUG_TOOL.get()))
            .displayItems((parameters, output) -> output.accept(ModItems.PROFESSION_DEBUG_TOOL.get()))
            .build());

    private ModTabs() {
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
