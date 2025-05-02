package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.util.FUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KickCommand extends KoolCommand
{
    /*public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
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
    }*/

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0)
        {
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null)
        {
            msg(sender, playerNotFound);
            return true;
        }

        String reason = args.length >= 2 ? String.join(" ", ArrayUtils.remove(args, 0)) : null;

        target.kick(FUtil.miniMessage("<red>You have been kicked from the server." +
                        "<newline>Kicked by: <yellow><sender></yellow><reason_if_present>",
                Placeholder.unparsed("sender", sender.getName()),
                Placeholder.component("reason_if_present", reason != null ?
                        FUtil.miniMessage("<newline>Reason: <yellow><reason></yellow>",
                                Placeholder.unparsed("reason", reason)) :
                        Component.empty())), PlayerKickEvent.Cause.KICK_COMMAND);

        FUtil.staffAction(sender, "Kicked <player>" + (reason != null ? ", Reason: <white><reason></white>" : ""),
                Placeholder.unparsed("player", target.getName()),
                Placeholder.unparsed("reason", reason != null ? reason : ""));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String commandLabel, String[] args)
    {
        return args.length == 1 ? Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(name -> !name.equalsIgnoreCase(sender.getName())).toList() : List.of();
    }
}
