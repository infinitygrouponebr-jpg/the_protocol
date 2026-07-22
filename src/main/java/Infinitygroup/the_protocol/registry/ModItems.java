package Infinitygroup.the_protocol.registry;

import Infinitygroup.the_protocol.SurvivalProfessionsMod;
import Infinitygroup.the_protocol.profession.ProfessionData;
import Infinitygroup.the_protocol.profession.ProfessionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SurvivalProfessionsMod.MOD_ID);
    public static final DeferredItem<Item> PROFESSION_DEBUG_TOOL = ITEMS.registerItem("profession_debug_tool", DebugToolItem::new);

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private static final class DebugToolItem extends Item {
        private DebugToolItem(Properties properties) {
            super(properties.stacksTo(1));
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                ProfessionData data = ProfessionManager.get(serverPlayer);
                player.displayClientMessage(Component.literal("Profession: " + data.profession() + " | Level: " + data.level() + " | XP: " + data.experience()), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
    }
}
