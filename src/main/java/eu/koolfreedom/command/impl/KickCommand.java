package eu.koolfreedom.command.impl;

import eu.koolfreedom.util.FUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KickCommand implements CommandExecutor
{
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.GRAY + "Usage: /<command> (player) [reason]");
            return true;
        }

        if (!sender.hasPermission("kf.admin"))
        {
            sender.sendMessage(Messages.MSG_NO_PERMS);
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null)
        {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND);
            return true;
        }

        StringBuilder message = new StringBuilder()
                .append(ChatColor.GOLD)
                .append("You've been kicked!")
                .append("\nKicked by: ")
                .append(ChatColor.RED)
                .append(sender.getName());

        String reason = Messages.NO_REASON;
        if (args.length > 1)
        {
            reason = StringUtils.join(args, " ", 1, args.length);
            message.append(ChatColor.GOLD)
                    .append("\nReason: ")
                    .append(ChatColor.RED)
                    .append(reason);
        }

        player.kickPlayer(message.toString());
        FUtil.adminAction(sender.getName(), "Kicking " + player.getName(), true);
        return true;
    }
}
