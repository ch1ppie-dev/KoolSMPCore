package eu.koolfreedom.command.impl;

import eu.koolfreedom.command.CommandParameters;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.stats.PlaytimeManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandParameters(name = "resyncplaytime", description = "Force import of Bukkit playtime data", usage = "/resyncplaytime <player>", permission = "kfc.resync")
public class PlaytimeSyncCommand extends KoolCommand
{
    private final PlaytimeManager playtimeManager = plugin.getPlaytimeManager();

    @Override
    public boolean run(CommandSender sender, Player player, Command cmd, String label, String[] args)
    {
        if (args.length != 1)
        {
            msg(sender, "<red>Usage: /resyncplaytime <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline()))
        {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null)
            {
                msg(sender, playerNotFound);
                return true;
            }
        }

        UUID uuid = target.getUniqueId();
        playtimeManager.importFromEssentials(uuid);
        playtimeManager.save();

        msg(sender, "<green>Resynced playtime from Bukkit statistics for <yellow>" + target.getName() + "</yellow>.");
        return true;
    }
}
