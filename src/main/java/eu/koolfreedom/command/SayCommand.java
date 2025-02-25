package eu.koolfreedom.command;

import eu.koolfreedom.util.FUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SayCommand implements CommandExecutor
{

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0)
        {
            return false;
        }

        String message = StringUtils.join(args, " ");

        FUtil.bcastMsg(String.format("[KoolFreedom:%s] %s", sender.getName(), message), ChatColor.LIGHT_PURPLE);

        return true;
    }
}