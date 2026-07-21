package Infinitygroup.the_protocol;

import Infinitygroup.the_protocol.command.TraitCommand;
import Infinitygroup.the_protocol.compat.CompatManager;
import Infinitygroup.the_protocol.data.ModAttachments;
import Infinitygroup.the_protocol.trait.TraitRegistry;
import Infinitygroup.the_protocol.trait.TraitService;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

@Mod(The_protocol.MODID)
public class The_protocol {
    public static final String MODID = "the_protocol";
    public static final Logger LOGGER = LogUtils.getLogger();

    public The_protocol(IEventBus modEventBus, ModContainer modContainer) {
        Config.register(modContainer);
        ModAttachments.register(modEventBus);
        TraitRegistry.bootstrap();
        modEventBus.addListener(Config::onConfigLoad);

        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(this::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(this::onLivingFall);
        NeoForge.EVENT_BUS.addListener(this::onArrowLoose);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        LOGGER.info("Loaded {} with TaCZ present: {} and vehicle compat ids: {}",
                MODID,
                CompatManager.isTaczLoaded(),
                CompatManager.describeVehicleCompatTargets());
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        TraitService.handlePlayerLogin(event.getEntity() instanceof ServerPlayer serverPlayer ? serverPlayer : null);
    }

    private void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        TraitService.handlePlayerRespawn(event.getEntity() instanceof ServerPlayer serverPlayer ? serverPlayer : null);
    }

    private void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        TraitService.handlePlayerLogout(event);
    }

    private void onPlayerTick(PlayerTickEvent.Post event) {
        TraitService.handlePlayerTick(event);
    }

    private void onLivingFall(LivingFallEvent event) {
        TraitService.handleLivingFall(event);
    }

    private void onArrowLoose(ArrowLooseEvent event) {
        TraitService.handleArrowLoose(event);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        TraitCommand.register(event.getDispatcher());
    }
}
