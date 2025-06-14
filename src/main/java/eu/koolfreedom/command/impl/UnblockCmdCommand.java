package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.command.CommandParameters;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.listener.MuteManager;
import eu.koolfreedom.util.FUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandParameters(name = "unblockcmd", description = "Unblocks commands for a player.", usage = "/<command> <player>", aliases = "unblockcommand,unblockcommands,ubcmds,unblockcmds,ubc")
public class UnblockCmdCommand extends KoolCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String s, String[] args)
    {
        MuteManager manager = KoolSMPCore.getInstance().getMuteManager();

        if (args.length == 0)
        {
            return false;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null)
        {
            msg(sender, playerNotFound);
            return true;
        }

        if (manager.isCommandsBlocked(player))
        {
            manager.setCommandsBlocked(player, false);
            FUtil.staffAction(sender, "Unblocking commands for <player>", Placeholder.unparsed("player", player.getName()));
            msg(sender, "<gray>Unblocked commands for <player>", Placeholder.unparsed("player", player.getName()));
        }
        else
        {
            msg(sender, "<red>That players commands aren't blocked.");
        }
        return true;
    }
}
