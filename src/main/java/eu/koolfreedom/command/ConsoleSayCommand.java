package eu.koolfreedom.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConsoleSayCommand {
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0)
        {
            return false;
        }

        String message = StringUtils.join(args, " ");
        Bukkit.broadcastMessage(String.format("§7[CONSOLE] §c%s §8\u00BB §f%s", sender.getName(), StringUtils.join(args, " ")));
        return true;
    }
}