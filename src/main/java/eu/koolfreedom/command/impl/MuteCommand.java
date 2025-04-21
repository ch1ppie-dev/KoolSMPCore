package eu.koolfreedom.command.impl;

import eu.koolfreedom.listener.MuteManager;
import eu.koolfreedom.punishment.PunishmentList;
import eu.koolfreedom.punishment.PunishmentType;
import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MuteCommand implements CommandExecutor
{

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender.hasPermission("kf.admin"))) {
            sender.sendMessage(Messages.MSG_NO_PERMS);
            return true;
        }

        if (args.length != 1)
        {
            return false;
        }
        if (args[0].equalsIgnoreCase("purge"))
        {
            int muted = MuteManager.getMutedAmount();
            MuteManager.wipeMutes();
            FUtil.adminAction(sender.getName(), "Unmuting all player", true);
            sender.sendMessage(ChatColor.GRAY + "Unmuted " + muted + " players.");
            return true;
        }
        Player player = Bukkit.getPlayer(args[0]);
        MuteManager.setMuted(player, !MuteManager.isMuted(player));
        if (MuteManager.isMuted(player))
        {
            // log mute
            PunishmentList.logPunishment(player, PunishmentType.MUTE, sender, null);
        }
        FUtil.adminAction(sender.getName(), (MuteManager.isMuted(player) ? "Muting " : "Unmuting ") + player.getName(), true);
        return true;
    }
}
