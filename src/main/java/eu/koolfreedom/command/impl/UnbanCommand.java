package eu.koolfreedom.command.impl;

import eu.koolfreedom.banning.BanList;
import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class UnbanCommand implements CommandExecutor
{
    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args)
    {
        if (!sender.hasPermission("kf.admin"))
        {
            sender.sendMessage(Messages.MSG_NO_PERMS);
            return true;
        }
        if (args.length == 0)
        {
            return false;
        }

        if (args.length > 1)
        {
            return false;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (!BanList.isBanned(player))
        {
            sender.sendMessage(ChatColor.GRAY + "That player is not banned.");
            return true;
        }
        FUtil.adminAction(sender.getName(), "Unbanning " + player.getName(), true);
        BanList.removeBan(player);
        return true;
    }
}
