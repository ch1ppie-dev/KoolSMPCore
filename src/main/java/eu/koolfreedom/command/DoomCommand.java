package eu.koolfreedom.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import org.jetbrains.annotations.NotNull;
import java.util.*;

@SuppressWarnings("deprecation")
public class DoomCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /" + s + " <player> [reason]", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Could not find the specified player on the server.", NamedTextColor.RED));
            return true;
        }
        if (!sender.hasPermission("kf.senior")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
            return false;
        }
        final Player player = Bukkit.getPlayer(args[0]);
        for (final Player onlinePlayers : Bukkit.getOnlinePlayers()) {
            onlinePlayers.sendMessage(ChatColor.RED + "" + sender + "- Swinging the Russian Hammer over " + player);
            onlinePlayers.sendMessage(ChatColor.RED + "" + player + " will be squashed!");
        }
        final Location playerLoc = player.getLocation();
        player.getWorld().strikeLightning(playerLoc);
        player.getWorld().strikeLightning(playerLoc);
        player.getWorld().strikeLightning(playerLoc);
        player.getWorld().strikeLightning(playerLoc);
        player.getWorld().strikeLightning(playerLoc);
        player.getWorld().strikeLightning(playerLoc);
        String reason = args.length > 1 ? " (" + String.join(" ", Arrays.copyOfRange(args, 1, args.length)) + ")" : "";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "banip" + player + "&cMay your worst nightmare come true, and may you suffer by the hands of your ruler." + reason);
        return true;
    }
}
