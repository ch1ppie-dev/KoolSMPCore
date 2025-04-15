package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.util.FUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class BanCommand implements CommandExecutor
{
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if(!sender.hasPermission("kf.admin"))
        {
            sender.sendMessage(Messages.MSG_NO_PERMS);
            return true;
        }

        if (args.length == 0)
        {
            sender.sendMessage(KoolSMPCore.main.mmDeserialize("<red>Usage: /<command> [player] (reason)"));
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if(target == null || (!target.hasPlayedBefore() && !target.isOnline() ))
        {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND);
            return true;
        }

        StringBuilder message = new StringBuilder()
                .append(ChatColor.GOLD)
                .append("You've been banned!")
                .append("\nBanned by: ")
                .append(ChatColor.RED)
                .append(sender.getName());

        String reason = Messages.NO_REASON;
        if (args.length > 1) {
            reason = StringUtils.join(args, " ", 1, args.length);
            message.append(ChatColor.GOLD)
                    .append("\nReason: ")
                    .append(ChatColor.RED)
                    .append(reason);
        }

        String appeal = ConfigEntry.SERVER_WEBSITE_OR_FORUM.getString();
        message.append(ChatColor.GOLD)
                .append("\nYou may appeal by DMing one of our staff members at ")
                .append(ChatColor.RED)
                .append(appeal);

        Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, null, sender.getName());

        // Only kick if the player is online
        if (target.isOnline()) {
            Player onlinePlayer = Bukkit.getPlayer(target.getName());
            if (onlinePlayer != null) {
                onlinePlayer.kickPlayer(message.toString());
            }
        }

        FUtil.adminAction(sender.getName(), "Banning " + target.getName(), true);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **" + sender.getName() + " - Banning " + target.getName() + "**");
        return true;
    }
}
