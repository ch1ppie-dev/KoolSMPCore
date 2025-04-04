package eu.koolfreedom.command.impl;

import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /pat <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND);
            return true;
        }

        String senderName = sender instanceof Player ? ((Player) sender).getName() : "Console";
        FUtil.bcastMsg(ChatColor.AQUA + senderName + " gave " + target.getName() + " a pat on the head.");
        return true;
    }
}
