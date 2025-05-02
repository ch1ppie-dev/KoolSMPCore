package eu.koolfreedom.command.impl;

import eu.koolfreedom.command.KoolCommand;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class KissCommand extends KoolCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args)
    {
        if (args.length != 1)
        {
            return false;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null)
        {
            msg(sender, playerNotFound);
            return true;
        }

        if (player.equals(playerSender))
        {
            msg(sender, "<red>You must be pretty desperate for love if you're trying to kiss yourself.");
            return true;
        }

        broadcast("<aqua><sender> gave <target> a kiss on the cheek. Awww! <dark_red><b>♥",
                Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("target", player.getName()));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String commandLabel, String[] args)
    {
        return args.length == 1 ? Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(name -> !name.equalsIgnoreCase(sender.getName())).toList() : List.of();
    }
}
