package eu.koolfreedom.command;

import eu.koolfreedom.KoolSMPCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class DoomCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /" + s + " <player> [reason]", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Could not find the specified player on the server.", NamedTextColor.RED));
            return true;
        }
        if (!sender.hasPermission("kf.senior")) {
            sender.sendMessage(NamedTextColor.RED + "You do not have permission to use this command");
            return false;
        }
        for (int i = 0; i < 30; i++) {
            target.getWorld().strikeLightningEffect(target.getLocation());
        }

        target.setFireTicks(200);
        target.setGameMode(GameMode.SURVIVAL);

        Bukkit.broadcast(Component.text(sender.getName() + " - Swinging the Russian Hammer over " + target.getName(), NamedTextColor.RED));
        Bukkit.getScheduler().runTaskLater(KoolSMPCore.main, () -> Bukkit.broadcast(Component.text(target.getName() + " will be completely squashed!", NamedTextColor.RED)), 20);
        Bukkit.getScheduler().runTaskLater(KoolSMPCore.main, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " clear");
            if (target.isOp()) target.setOp(false);
        }, 20);
        Bukkit.getScheduler().runTaskLater(KoolSMPCore.main, () -> Bukkit.broadcast(Component.text(sender.getName() + " - Removing " + target.getName() + " from the staff list", NamedTextColor.RED)), 20);

        Bukkit.getScheduler().runTaskLater(KoolSMPCore.main, () -> target.setHealth(0), 10);

        String reason = args.length > 1 ? " (" + String.join(" ", Arrays.copyOfRange(args, 1, args.length)) + ")" : "";
        Bukkit.getScheduler().runTaskLater(KoolSMPCore.main, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ipban" + target.getName() + "&cMay your worst nightmare come true, and may you suffer by the hands of your ruler." + reason), 30);
        return true;
    }
}
