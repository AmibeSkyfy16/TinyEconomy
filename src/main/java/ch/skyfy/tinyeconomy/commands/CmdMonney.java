package ch.skyfy.tinyeconomy.commands;

import ch.skyfy.tinyeconomy.features.Economy;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CmdMonney {
    public static void registerMonneyCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("monney").executes(context -> {
            var playerMonney = Economy.getInstance().getBalance(context.getSource().getPlayer().getUuidAsString());
            context.getSource().getPlayer().sendMessage(Text.of("Votre capital est de : " + playerMonney + " Kukukiol"), false);
            return Command.SINGLE_SUCCESS;
        }));
    }
}
