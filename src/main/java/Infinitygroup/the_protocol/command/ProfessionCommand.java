package Infinitygroup.the_protocol.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import Infinitygroup.the_protocol.config.CommonConfig;
import Infinitygroup.the_protocol.profession.ProfessionData;
import Infinitygroup.the_protocol.profession.ProfessionManager;
import Infinitygroup.the_protocol.profession.ProfessionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ProfessionCommand {
    private ProfessionCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("profession")
                .then(Commands.literal("get").executes(context -> get(context.getSource())))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("profession", StringArgumentType.word())
                                        .executes(context -> set(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "profession"))))))
                .then(Commands.literal("addxp")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> addXp(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"))))))
                .then(Commands.literal("reset")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> reset(context.getSource(), EntityArgument.getPlayer(context, "player"))))));
    }

    private static int get(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }
        sendStatus(source, player);
        return 1;
    }

    private static int set(CommandSourceStack source, ServerPlayer player, String professionName) {
        if (!canUseDebugCommands(source)) return 0;
        ProfessionType profession = ProfessionType.fromName(professionName);
        if (!profession.name().equalsIgnoreCase(professionName)) {
            source.sendFailure(Component.literal("Unknown profession: " + professionName));
            return 0;
        }
        ProfessionManager.setProfession(player, profession);
        source.sendSuccess(() -> Component.literal("Set " + player.getName().getString() + " to " + profession + "."), true);
        return 1;
    }

    private static int addXp(CommandSourceStack source, ServerPlayer player, int amount) {
        if (!canUseDebugCommands(source)) return 0;
        ProfessionManager.addExperience(player, amount);
        sendStatus(source, player);
        return 1;
    }

    private static int reset(CommandSourceStack source, ServerPlayer player) {
        if (!canUseDebugCommands(source)) return 0;
        ProfessionManager.reset(player);
        source.sendSuccess(() -> Component.literal("Reset " + player.getName().getString() + "'s profession."), true);
        return 1;
    }

    private static boolean canUseDebugCommands(CommandSourceStack source) {
        if (!CommonConfig.ENABLE_DEBUG_COMMANDS.get() || !source.hasPermission(2)) {
            source.sendFailure(Component.literal("Profession debug commands are disabled or require permission level 2."));
            return false;
        }
        return true;
    }

    public static void sendStatus(CommandSourceStack source, ServerPlayer player) {
        ProfessionData data = ProfessionManager.get(player);
        source.sendSuccess(() -> Component.literal(player.getName().getString() + ": " + data.profession() + " | Level " + data.level() + " | XP " + data.experience() + " | Perk points " + data.perkPoints()), false);
    }
}
