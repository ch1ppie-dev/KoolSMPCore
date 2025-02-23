package eu.koolfreedom.command;

import eu.koolfreedom.KoolSMPCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class SmiteCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender.hasPermission("kf.admin"))) {
            sender.sendMessage(ChatColor.RED + "Unknown command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /smite <player> [reason]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        // Strike lightning 8 times
        for (int i = 0; i < 8; i++) {
            target.getWorld().strikeLightningEffect(target.getLocation());
        }

        // Apply smite effects
        target.setHealth(0);
        target.setGameMode(GameMode.SURVIVAL);
        target.setOp(false);

        Bukkit.getScheduler().runTaskLater(KoolSMPCore.main, () -> {
            KoolSMPCore.bcastMsg(ChatColor.RED + target.getName() + " has been a naughty, naughty child.");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord broadcast **" + target.getName() + " has been a naughty, naughty child.**");
            KoolSMPCore.bcastMsg(ChatColor.RED + "Smitten by: " + ChatColor.YELLOW + sender.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord broadcast **Smitten by**: " + sender.getName());
        }, 1L);

        if (args.length > 1) {
            String reason = String.join(" ", args).substring(args[0].length()).trim();
            KoolSMPCore.bcastMsg(ChatColor.RED + "Reason: " + ChatColor.YELLOW + reason);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord broadcast **Reason**: " + reason);
        }

        return true;
    }
}

