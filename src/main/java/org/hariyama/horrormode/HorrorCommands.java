package org.hariyama.horrormode;

import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HorrorMode.ID)
public final class HorrorCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        var root = Commands.literal("horror").requires(source -> source.hasPermission(2));
        String[] names = {"darkness", "message", "npc", "watcher", "mood", "sound"};
        for (int i = 0; i < names.length; i++) {
            final int action = i;
            root.then(Commands.literal(names[i])
                    .executes(ctx -> run(ctx.getSource(), List.of(ctx.getSource().getPlayerOrException()), action))
                    .then(Commands.argument("players", EntityArgument.players())
                            .executes(ctx -> run(ctx.getSource(), EntityArgument.getPlayers(ctx, "players"), action))));
        }
        event.getDispatcher().register(root);
    }
    private static int run(CommandSourceStack source, Collection<ServerPlayer> players, int action) {
        if (!source.hasPermission(2)) return 0;
        int count = 0;
        for (var player : players) {
            if (!player.isAlive() || player.isSpectator() || (action == 2 && player.isCreative())) {
                source.sendFailure(Component.literal("Нельзя вызвать это событие для " + player.getScoreboardName()
                        + ": нужен живой игрок вне режима зрителя; для нападения — выживание или приключение."));
                continue;
            }
            if (HorrorEvents.trigger(player, action)) count++;
            else source.sendFailure(Component.literal("Не удалось создать NPC для " + player.getScoreboardName()
                    + ": нет подходящего места или NPC уже рядом."));
        }
        final int total = count;
        if (count > 0) source.sendSuccess(() -> Component.literal("Событие вызвано для игроков: " + total), true);
        return count;
    }
}
