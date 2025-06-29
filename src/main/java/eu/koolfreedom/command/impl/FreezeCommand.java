package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.command.CommandParameters;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.freeze.FreezeManager;
import eu.koolfreedom.util.FUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandParameters(name = "freeze", description = "Freezes specific players or all players", usage = "/<command> <player> <seconds> | <global on|off>")
public class FreezeCommand extends KoolCommand
{
    private final FreezeManager fm;

    public FreezeCommand()
    {
        this.fm = KoolSMPCore.getInstance().getFreezeManager();
    }

    @Override
    public boolean run(CommandSender sender, Player player, Command cmd, String s, String[] args)
    {
        if (args.length == 0)
        {
            return false;
        }

        if (args[0].equalsIgnoreCase("global"))
        {
            if (args.length < 2)
            {
                msg(sender, "<gray>Usage: /freeze global <on|off>");
                return true;
            }
            boolean on = args[1].equalsIgnoreCase("on");
            fm.setGlobalFreeze(on);
            FUtil.staffAction(sender, (on ? "F" : "Unf") + "roze all players");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(target == null)
        {
            msg(sender, playerNotFound);
            return true;
        }

        if(fm.isFrozen(target))
        {
            fm.unfreeze(target);
            FUtil.staffAction(sender, "Unfroze <player>", Placeholder.unparsed("player", target.getName()));
            msg(sender, "<gray>Unfroze <player>", Placeholder.unparsed("player", target.getName()));
            msg(target, "<gray>You have been unfrozen");
            return true;
        }

        long secs = args.length >= 2 ? parseLong(args[1], 300) : 300;
        fm.freeze(target, secs);
        FUtil.staffAction(sender, "Froze <player>", Placeholder.unparsed("player", target.getName()));
        msg(sender, "<aqua>You have frozen <player>", Placeholder.unparsed("player", target.getName()));
        msg(target, "<gray>You have been <aqua>frozen</aqua> by an admin");
        return true;
    }

    private long parseLong(String s, long def) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return def; }
    }
}
