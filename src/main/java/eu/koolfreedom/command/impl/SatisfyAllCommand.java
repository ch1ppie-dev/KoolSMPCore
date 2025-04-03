package eu.koolfreedom.command.impl;

import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SatisfyAllCommand implements CommandExecutor
{
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args)
    {
        if (!sender.hasPermission("kf.admin"))
        {
            sender.sendMessage(Messages.MSG_NO_PERMS);
            return true;
        }
        for (Player player : Bukkit.getOnlinePlayers())
        {
            player.setHealth(20.0);
            player.setFoodLevel(20);
        }
        FUtil.adminAction(sender.getName(), "Healing all players", false);
        return true;
    }
}