package Infinitygroup.the_protocol.command;

import Infinitygroup.the_protocol.trait.TraitDefinition;
import Infinitygroup.the_protocol.trait.TraitRegistry;
import Infinitygroup.the_protocol.trait.TraitService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

public final class TraitCommand {
    private TraitCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("trait")
                .then(Commands.literal("info").executes(context -> info(context.getSource().getPlayerOrException())))
                .then(Commands.literal("reroll").executes(context -> reroll(context.getSource().getPlayerOrException())))
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("trait", StringArgumentType.word())
                                        .suggests(TraitCommand::suggestTraits)
                                        .executes(context -> setTrait(context)))))
                .then(Commands.literal("clear")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> clearTrait(context))))
                .then(Commands.literal("debug")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> debug(context.getSource()))));
    }

    private static int info(ServerPlayer player) {
        var data = TraitService.getData(player);
        var currentTrait = TraitService.currentTrait(player);
        player.displayClientMessage(Component.translatable("message.the_protocol.trait.info.header"), false);
        player.displayClientMessage(Component.translatable("message.the_protocol.trait.info.state",
                currentTrait.map(TraitDefinition::displayName).orElse(Component.translatable("message.the_protocol.trait.info.none")),
                data.initialGranted(),
                data.rerollUsed()), false);
        return 1;
    }

    private static int reroll(ServerPlayer player) {
        TraitService.rerollTrait(player);
        return 1;
    }

    private static int setTrait(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        String traitIdValue = StringArgumentType.getString(context, "trait");
        ResourceLocation traitId = ResourceLocation.tryParse(traitIdValue);
        if (traitId == null || TraitRegistry.get(traitId).isEmpty()) {
            context.getSource().sendFailure(Component.translatable("message.the_protocol.trait.invalid_trait", traitIdValue));
            return 0;
        }

        TraitDefinition trait = TraitRegistry.get(traitId).orElse(null);
        if (trait == null || !TraitService.setTrait(player, traitId)) {
            context.getSource().sendFailure(Component.translatable("message.the_protocol.trait.invalid_trait", traitIdValue));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.translatable("message.the_protocol.trait.set", player.getDisplayName(), trait.displayName()), true);
        return 1;
    }

    private static int clearTrait(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        TraitService.clearTrait(player);
        context.getSource().sendSuccess(() -> Component.translatable("message.the_protocol.trait.cleared", player.getDisplayName()), true);
        return 1;
    }

    private static int debug(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Trait debug: taczLoaded=" + Infinitygroup.the_protocol.compat.CompatManager.isTaczLoaded() + ", vehicleLoaded=" + Infinitygroup.the_protocol.compat.CompatManager.isVehicleCompatLoaded()), false);
        for (TraitDefinition trait : TraitRegistry.allTraits()) {
            source.sendSuccess(() -> Component.literal("- " + trait.id() + " | " + trait.displayName().getString() + " | available=" + TraitService.availableTraits().contains(trait)), false);
        }
        return 1;
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestTraits(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(TraitRegistry.allTraits().stream().map(trait -> trait.id().toString()), builder);
    }
}
