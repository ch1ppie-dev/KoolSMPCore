package eu.koolfreedom.command;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SmiteCommand implements CommandExecutor 
{
    public static void smite(CommandSender sender, Player player)
    {
        smite(sender, player, null, false, false);
    }

    public static void smite(CommandSender sender, Player player, String reason)
    {
        smite(sender, player, reason, false, false)
    }

    
}