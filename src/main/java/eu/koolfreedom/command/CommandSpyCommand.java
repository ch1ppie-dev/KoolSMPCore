package eu.koolfreedom.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class CommandSpyCommand implements CommandExecutor, Listener {
    private final Set<UUID> commandSpyEnabled = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (!player.hasPermission("kf.admin")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cKoolSurvival&8] &4Fuck you bud you can't do this."));
            return true;
        }

        if (commandSpyEnabled.contains(playerUUID)) {
            commandSpyEnabled.remove(playerUUID);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b&lCommand Spy &4&ldiabled!"));
        } else {
            commandSpyEnabled.add(playerUUID);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b&lCommand Spy &a&lenabled!"));
        }
        return true;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player sender = event.getPlayer();
        String commandMessage = event.getMessage();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (commandSpyEnabled.contains(onlinePlayer.getUniqueId()) && !onlinePlayer.equals(sender)) {
                onlinePlayer.sendMessage(ChatColor.GRAY + sender.getName() + ": " + ChatColor.GRAY + commandMessage);
            }
        }
    }
}
