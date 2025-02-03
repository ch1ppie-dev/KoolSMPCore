package eu.koolfreedom.command;

import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import java.util.*;

@SuppressWarnings("deprecation")
public class DoomCommand implements CommandExecutor {
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
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
