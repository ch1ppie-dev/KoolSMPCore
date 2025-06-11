package eu.koolfreedom.command.impl;

import eu.koolfreedom.command.CommandParameters;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.util.FUtil;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

import static org.bukkit.Bukkit.getPlayer;

@CommandParameters(name = "cage", description = "For the naughty ones, put them in a cage", usage = "/<command> <player>")
public class CageCommand extends KoolCommand
{
    @Getter
    Set<UUID> cagedPlayers = getCagedPlayers();

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String s, String[] args)
    {

        if (args.length == 0)
        {
            return false;
        }

        if (args[0].equalsIgnoreCase("purge"))
        {
            FUtil.staffAction(sender, "Uncaging all players");
            for (UUID uuid : new HashSet<>(cagedPlayers))
            {
                Player player = getPlayer(uuid);
                if (player != null)
                {
                    uncagePlayer(player);
                }
            }
            cagedPlayers.clear();
            return true;
        }

        Player target = getPlayer(args[0]);
        if (target == null)
        {
            msg(sender, playerNotFound);
            return true;
        }

        if (cagedPlayers.contains(target.getUniqueId()))
        {
            msg(sender, "<red>That player is already muted");
            return true;
        }

        Material outer = Material.GLASS;
        Material inner = Material.AIR;
        String skullName = null;

        if (args.length >= 2)
        {
            switch (args[1].toLowerCase())
            {
                case "head":
                    outer = Material.PLAYER_HEAD;
                    if (args.length >= 3)
                        skullName = args[2];
                    break;
                case "block":
                    if (args.length >= 3)
                    {
                        Material mat = Material.matchMaterial(args[2]);
                        if (mat != null && mat.isBlock())
                        {
                            outer = mat;
                        }
                        else
                        {
                            msg(sender, "<red>Invalid block");
                            return true;
                        }
                    }
                    else return false;
                    break;
                default:
                    return false;
            }
        }

        target.setGameMode(GameMode.SURVIVAL);
        buildCage(target, outer, inner, skullName);
        cagedPlayers.add(target.getUniqueId());

        FUtil.staffAction(sender, "Caging <player>", Placeholder.unparsed("player", target.getName()));
        return true;
    }

    private void buildCage(Player player, Material outer, Material inner, String skullName)
    {
        Location base = player.getLocation().getBlock().getLocation();

        for (int x = -1; x <= 1; x++)
        {
            for (int y = 0; y <= 2; y++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    Location loc = base.clone().add(x, y, z);
                    boolean isInner = x == 0 && y == 1 && z == 0;

                    if (isInner)
                    {
                        loc.getBlock().setType(inner);
                    }
                    else
                    {
                        if (outer == Material.PLAYER_HEAD && skullName != null)
                        {
                            loc.getBlock().setType(Material.PLAYER_HEAD);
                            Skull skull = (Skull) loc.getBlock().getState();
                            skull.setOwningPlayer(Bukkit.getOfflinePlayer(skullName));
                            skull.update();
                        }
                        else
                        {
                            loc.getBlock().setType(outer);
                        }
                    }
                }
            }
        }
    }

    private void uncagePlayer(Player player)
    {
        Location base = player.getLocation().getBlock().getLocation();

        for (int x = -1; x <= 1; x++)
        {
            for (int y = 0; y <= 2; y++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    Location loc = base.clone().add(x, y, z);
                    loc.getBlock().setType(Material.AIR);
                }
            }
        }
    }
}
