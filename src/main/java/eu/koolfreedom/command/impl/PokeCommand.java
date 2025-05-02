package eu.koolfreedom.command.impl;

import eu.koolfreedom.command.KoolCommand;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class PokeCommand extends KoolCommand
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
            msg(sender, "<red>You accidentally poked yourself in the eye.");
            player.damage(playerSender.getHealth() / 10);
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 120, 255, false, false, true));
            return true;
        }

        broadcast("<aqua><sender> poked <target>.",
                Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("target", player.getName()));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String commandLabel, String[] args)
    {
        return args.length == 1 ? Bukkit.getOnlinePlayers().stream().map(Player::getName).toList() : List.of();
    }
}
