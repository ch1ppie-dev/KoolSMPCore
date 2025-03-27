package eu.koolfreedom.command;

import eu.koolfreedom.punishment.Punishment;
import eu.koolfreedom.punishment.PunishmentManager;
import eu.koolfreedom.punishment.PunishmentType;
import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class MuteCommand implements CommandExecutor
{
    private final PunishmentManager punishmentManager;

    public MuteCommand(PunishmentManager punishmentManager) {
        this.punishmentManager = punishmentManager;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender.hasPermission("kf.admin"))) {
            sender.sendMessage(Messages.MSG_NO_PERMS);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /mute <player> [reason]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND);
            return true;
        }

        UUID targetUUID = target.getUniqueId();
        String reason = args.length > 1 ? " (" + String.join(" ", Arrays.copyOfRange(args, 1, args.length)) + ")" : "";

        Punishment punishment = new Punishment(targetUUID, PunishmentType.MUTE, reason, -1);
        punishmentManager.addPunishment(targetUUID, "MUTE", reason, -1);

        FUtil.adminAction(sender.getName(), "Muting " + target.getName(), true);

        return true;
    }
}
