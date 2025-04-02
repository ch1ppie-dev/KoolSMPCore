package eu.koolfreedom.command;

import eu.koolfreedom.util.FUtil;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UnbanIPCommand implements CommandExecutor
{
    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.GRAY + "Usage: /<command> (player)");
            return false;
        }

        if (!sender.hasPermission("kf.admin"))
        {
            sender.sendMessage(Messages.MSG_NO_PERMS);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        String ip = target.getAddress().getAddress().getHostAddress(); // Get the player's IP
        if (ip == null) {
            sender.sendMessage(ChatColor.RED + "Could not retrieve player's IP address.");
            return true;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if (!player.isBanned())
        {
            sender.sendMessage(ChatColor.GRAY + "That player is not banned");
            return true;
        }


        Bukkit.getBanList(BanList.Type.IP).pardon(ip);
        FUtil.adminAction(sender.getName(), "Unbanning an IP address", true);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **" + sender.getName() + " - Unbanning an IP address");
        sender.sendMessage(ChatColor.GRAY + "IP Address " + ip + " has been unbanned");
        return true;
    }
}
