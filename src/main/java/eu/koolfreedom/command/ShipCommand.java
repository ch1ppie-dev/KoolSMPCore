package eu.koolfreedom.command;

import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class ShipCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ship <player1> <player2>");
            return true;
        }

        Player p1 = Bukkit.getPlayer(args[0]);
        Player p2 = Bukkit.getPlayer(args[1]);
        Player playerSender = (Player) sender;

        if (p1 == null || p2 == null || !p1.isOnline() || !p2.isOnline()) {
            sender.sendMessage(ChatColor.RED + "One or both players not found!");
            return true;
        }

        if (p1.equals(p2)) {
            sender.sendMessage(ChatColor.RED + "You can't ship someone with themselves! Here's a cookie for trying.");
            ItemStack cookie = new ItemStack(Material.COOKIE);
            ItemMeta meta = cookie.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + "Idiot of the week award");
            cookie.setItemMeta(meta);
            playerSender.getInventory().addItem(cookie);
            return true;
        }

        FUtil.bcastMsg(ChatColor.GREEN + sender.getName() + " ships " + p1.getName() + " x " + p2.getName() + ChatColor.LIGHT_PURPLE + " <3");
        return true;
    }
}
