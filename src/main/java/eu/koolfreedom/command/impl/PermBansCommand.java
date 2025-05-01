package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.banning.PermBans;
import eu.koolfreedom.log.FLog;
import eu.koolfreedom.util.KoolSMPCoreBase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PermBansCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, String[] args)
    {
        if (args.length == 0)
        {
            sender.sendMessage(KoolSMPCore.main.mmDeserialize("Usage: /" + str + " <reload>"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload"))
        {
            if (!sender.hasPermission("kf.senior"))
            {
                sender.sendMessage(Messages.MSG_NO_PERMS);
                return true;
            }
            try
            {
                PermBans.getConfig().reload();
                sender.sendMessage(KoolSMPCore.main.mmDeserialize("<green>Reloaded PermBans config."));
                return true;
            }
            catch (Exception ex)
            {
                FLog.error("Failed to reload permban configuration", ex);
                sender.sendMessage(Messages.FAILED);
            }
            return true;
        }
        return false;
    }
}
