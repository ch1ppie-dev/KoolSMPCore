package eu.koolfreedom.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CrashCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command sender has permission
        if (!sender.hasPermission("kf.senior")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        // Check if the command is run by a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player executor = (Player) sender;

        if (args.length == 0) {
            // Crash all players except the executor
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.equals(executor)) {
                    crashPlayer(player);
                }
            }
            executor.sendMessage(ChatColor.GREEN + "Crashed all players on the server.");
        } else if (args.length == 1) {
            // Crash a specific player
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                executor.sendMessage(ChatColor.RED + "Player not found or not online.");
                return true;
            }
            if (target.equals(executor)) {
                executor.sendMessage(ChatColor.RED + "You cannot crash yourself.");
                return true;
            }

            crashPlayer(target);
            executor.sendMessage(ChatColor.GREEN + "Crashed player: " + target.getName());
        } else {
            executor.sendMessage(ChatColor.RED + "Usage: /crash [player]");
        }

        return true;
    }

    private void crashPlayer(Player player) {
        // Send a particle overload to crash the player's client
        String command = "execute at " + player.getName() + " run particle ash ~ ~ ~ 1 1 1 1 2147483647 force " + player.getName();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        player.sendMessage(ChatColor.RED + "You have been crashed!");
    }
}
