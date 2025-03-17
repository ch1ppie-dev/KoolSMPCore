package eu.koolfreedom.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import eu.koolfreedom.util.FUtil;
import org.jetbrains.annotations.NotNull;

public class AdminChatCommand implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args)
    {
        if (!sender.hasPermission("kf.admin"))
        {
            sender.sendMessage(Messages.MSG_NO_PERMS);
            return true;
        }
        if (args.length == 0)
        {
            sender.sendMessage(Messages.MISSING_ARGS);
            return true;
        }
        String message = StringUtils.join(args, " ");
        FUtil.adminChat(sender, message);
        return true;
    }
}
