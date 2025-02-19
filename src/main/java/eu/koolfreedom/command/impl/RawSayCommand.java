package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;


public class RawSayCommand implements CommandExecutor
{
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args)
    {
        if (args.length == 0)
        {
            KoolSMPCore.bcastMsg(KoolSMPCore.colorize(StringUtils.join(args, " ")));
        }

        return true;
    }
}
