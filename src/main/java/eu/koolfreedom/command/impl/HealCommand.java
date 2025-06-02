package eu.koolfreedom.command.impl;

import eu.koolfreedom.command.CommandParameters;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.util.FUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandParameters(name = "heal", description = "Heals the specified player", usage = "/<command> <player>")
public class HealCommand extends KoolCommand
{
    /*
    /
    / If a server has WorldGuard and Essentials installed, WorldGuard's piss-poor /heal takes priority over Essentials
    / This aims to take priority over both, while doing some of the same things that Essentials does.
    /
    / If WorldGuard still overrides this one, change the alias of "heal" to "koolsmpcore:heal" in commands.yml in the root of your server folder.
    /
    /
    / This is also here in the event where a server owner - for some unknown reason - uses everything else BUT Essentials....
    /
    */
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String s, String[] args)
    {
        if (args.length == 0)
        {
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null)
        {
            msg(sender, playerNotFound);
            return true;
        }

        target.setHealth(20);
        target.setFoodLevel(20);
        target.setSaturation(20);
        target.getActivePotionEffects().forEach(effect -> target.removePotionEffect(effect.getType()));
        target.setFireTicks(0);
        FUtil.staffAction(sender, "Healed <player>", Placeholder.unparsed("player", target.getName()));
        return true;
    }
}
