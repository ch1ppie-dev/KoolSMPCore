package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpectateCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args)
    {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.ONLY_IN_GAME);
            return true;
        }

        if (args.length == 0)
        {
            sender.sendMessage(KoolSMPCore.main.mmDeserialize("<red>Usage: /" + s + " <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null)
        {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND);
            return true;
        }

        if (target.getGameMode().equals(GameMode.SPECTATOR))
        {
            sender.sendMessage(KoolSMPCore.main.mmDeserialize("<red>You cannot spectate other players who are in spectator"));
            return true;
        }

        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(target);

        player.sendMessage(KoolSMPCore.main.mmDeserialize("<green>Now spectating " + target.getName()));
        return true;
    }
}