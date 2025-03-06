package eu.koolfreedom.command;

import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("all")
public class CryCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        String senderName = sender instanceof Player ? ((Player) sender).getName() : "Console";
        FUtil.bcastMsg(ChatColor.AQUA + senderName + " has started to cry :(");
        return true;
    }
}
