package eu.koolfreedom.command.impl;

import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.command.CommandParameters;
import eu.koolfreedom.freeze.FreezeManager;
import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandParameters(name = "freeze", description = "Freeze players", usage = "/freeze <player> [seconds] | global on|off")
public class FreezeCommand extends KoolCommand
{
    private final FreezeManager manager = KoolSMPCore.getInstance().getFreezeManager();

    @Override
    public boolean run(CommandSender sender, Player player, Command command, String label, String[] args) {
        if (args.length == 0) return false;

        if (args[0].equalsIgnoreCase("global"))
        {
            if (args.length < 2)
            {
                msg(sender, "<gray>Usage: /freeze global <on|off>");
                return true;
            }
            boolean on = args[1].equalsIgnoreCase("on");
            manager.setGlobalFreeze(on);
            FUtil.staffAction(sender, (on ? "Globally froze" : "Unfroze") + " all players");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null)
        {
            msg(sender, playerNotFound);
            return true;
        }

        UUID uuid = target.getUniqueId();

        if (manager.isFrozen(target))
        {
            manager.unfreeze(target);
            FUtil.staffAction(sender, "Unfroze " + target.getName());
            msg(sender, "<gray>Unfroze " + target.getName());
            plugin.getAutoUndoManager().cancelAutoUnfreeze(uuid);
            return true;
        }

        long seconds = args.length >= 2 ? parseLong(args[1], 300) : 300;
        manager.freeze(target, seconds);
        FUtil.staffAction(sender, "Froze " + target.getName() + " for " + seconds + "s");
        msg(sender, "<gray>Froze " + target.getName() + " for " + seconds + "s");
        plugin.getAutoUndoManager().scheduleAutoUnfreeze(target);
        return true;
    }

    private long parseLong(String s, long def)
    {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
