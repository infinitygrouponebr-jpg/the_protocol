package Infinitygroup.the_protocol;

import Infinitygroup.the_protocol.command.ProfessionCommand;
import Infinitygroup.the_protocol.compat.CompatManager;
import Infinitygroup.the_protocol.config.CommonConfig;
import Infinitygroup.the_protocol.event.PlayerProfessionEvents;
import Infinitygroup.the_protocol.event.CombatRollProfessionEvents;
import Infinitygroup.the_protocol.compat.CombatRollCompat;
import Infinitygroup.the_protocol.registry.ModDataComponents;
import Infinitygroup.the_protocol.registry.ModItems;
import Infinitygroup.the_protocol.registry.ModTabs;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(SurvivalProfessionsMod.MOD_ID)
public final class SurvivalProfessionsMod {
    public static final String MOD_ID = "the_protocol";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SurvivalProfessionsMod(IEventBus modEventBus, ModContainer modContainer) {
        CommonConfig.register(modContainer);
        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModTabs.register(modEventBus);

        NeoForge.EVENT_BUS.register(PlayerProfessionEvents.class);
        NeoForge.EVENT_BUS.register(CombatRollProfessionEvents.class);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        modEventBus.addListener(this::onConfigReload);

        LOGGER.info("{} loaded. TaCZ: {}, vehicle mods: {}, MicroTech: {}", MOD_ID,
                CompatManager.isTaczLoaded(), CompatManager.isVehicleModLoaded(), CompatManager.isMicroTechLoaded());
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ProfessionCommand.register(event.getDispatcher());
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != CommonConfig.SPEC || ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }
        ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(CombatRollCompat::refreshRollPermission);
    }
}
