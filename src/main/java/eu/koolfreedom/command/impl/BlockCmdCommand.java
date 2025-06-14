package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.command.CommandParameters;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.listener.MuteManager;
import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getPlayer;

@CommandParameters(name = "blockcommand", description = "Block all commands for everyone on the server, or a specific player.", usage = "/<command> <-a | purge | <player>>", aliases = {"bmcd", "blockcmd"})
public class BlockCmdCommand extends KoolCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String s, String[] args)
    {
        MuteManager manager = KoolSMPCore.getInstance().getMuteManager();

        if (args.length != 1)
        {
            return false;
        }

        String sub = args[0];

        if (sub.equalsIgnoreCase("purge"))
        {
            FUtil.staffAction(sender, "Unblocking commands for all players.");
            int count = manager.wipeBlockedCommands();

            // TODO: don't make this broadcast to everyone, only players who had their commands blocked
            //for (Player p : Bukkit.getOnlinePlayers())
            //{
            //    p.sendMessage(FUtil.miniMessage("<green>Your command block has been lifted."));
            //}

            msg(sender, "<gray>Unblocked commands for " + count + " players.");
            return true;
        }

        if (sub.equalsIgnoreCase("-a"))
        {
            FUtil.staffAction(sender, "Blocking commands for all non-admins.");
            int count = 0;

            for (Player p : Bukkit.getOnlinePlayers())
            {
                if (!p.hasPermission("kfc.admin"))
                {
                    if (!manager.isCommandsBlocked(p))
                    {
                        manager.setCommandsBlocked(p, true);
                        p.sendMessage(FUtil.miniMessage("<red>Your commands have been blocked by an admin."));
                        count++;
                    }
                }
            }

            msg(sender, "<gray>Blocked commands for " + count + " players.");
            return true;
        }

        Player target = getPlayer(sub);
        if (target == null)
        {
            msg(sender, playerNotFound);
            return true;
        }

        if (target.hasPermission("kfc.admin"))
        {
            msg(sender, "<gray>" + target.getName() + " is an admin and cannot be command-blocked.");
            return true;
        }

        if (manager.isCommandsBlocked(target))
        {
            msg(sender, "<red>That player's commands have already been blocked.");
            return true;
        }

        FUtil.staffAction(sender, "Blocking all commands for " + target.getName());
        manager.setCommandsBlocked(target, true);
        target.sendMessage(FUtil.miniMessage("<red>Your commands have been blocked."));
        msg(sender, "<gray>Blocked commands for " + target.getName() + ".");
        return true;
    }
}
