package eu.koolfreedom.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpectateCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player;
        if (commandSender instanceof Player) {
            player = (Player)commandSender;
        } else {
            commandSender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /" + s + " <player>", NamedTextColor.RED));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            commandSender.sendMessage(Component.text("Could not find the specified player on the server.", NamedTextColor.RED));
            return true;
        }
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(target);
        player.sendMessage(Component.text("Now spectating " + target.getName() + ".", NamedTextColor.GREEN));
        return true;
    }
}