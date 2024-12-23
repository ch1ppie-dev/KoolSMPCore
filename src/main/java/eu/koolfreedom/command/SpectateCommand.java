package eu.koolfreedom.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpectateCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player;
        if (commandSender instanceof Player) {
            player = (Player)commandSender;
        } else {
            commandSender.sendMessage((Component)Component.text("Only players can use this command.", (TextColor)NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage((Component)Component.text("Usage: /" + s + " <player>", (TextColor)NamedTextColor.RED));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            commandSender.sendMessage((Component)Component.text("Could not find the specified player on the server.", (TextColor)NamedTextColor.RED));
            return true;
        }
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport((Entity)target);
        player.sendMessage((Component)Component.text("Now spectating " + target.getName() + ".", (TextColor)NamedTextColor.GREEN));
        return true;
    }
}