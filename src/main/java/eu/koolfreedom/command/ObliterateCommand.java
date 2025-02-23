package eu.koolfreedom.command;

import eu.koolfreedom.KoolSMPCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ObliterateCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(Component.text("Usage: /" + s + " <player> [reason]", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            commandSender.sendMessage(Messages.PLAYER_NOT_FOUND);
            return true;
        }

        for (int i = 0; i < 30; i++) {
            target.getWorld().strikeLightningEffect(target.getLocation());
        }

        target.setFireTicks(200);
        target.setGameMode(GameMode.SURVIVAL);

        Bukkit.broadcast(Component.text(commandSender.getName() + " is unleashing Majora's Wrath upon " + target.getName() + "!", NamedTextColor.RED));
        Bukkit.getScheduler().runTaskLater(KoolSMPCore.main, () -> Bukkit.broadcast(Component.text(target.getName() + " will never see the light of day", NamedTextColor.RED)), 2);

        Bukkit.getScheduler().runTaskLater(KoolSMPCore.main, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " clear");
            if (target.isOp()) target.setOp(false);
        }, 2);

        Bukkit.getScheduler().runTaskLater(KoolSMPCore.main, () -> target.setHealth(0), 10);

        Bukkit.getScheduler().runTaskLater(KoolSMPCore.main, () -> Bukkit.broadcast(Component.text(target.getName() + " has been eradicated from existence!", NamedTextColor.RED)), 30);
        for (int i = 0; i < 3; i++) {
            Bukkit.getScheduler().runTaskLater(KoolSMPCore.main, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute as \"" + target.getName() + "\" at @s run particle flame ~ ~ ~ 1 1 1 1 999999999 force @s"), 30);
        }

        String reason = args.length > 1 ? " (" + String.join(" ", Arrays.copyOfRange(args, 1, args.length)) + ")" : "";
        Bukkit.getScheduler().runTaskLater(KoolSMPCore.main, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "banip " + target.getName() + " &4&lGet fucked " + reason + " | PERMANENT BAN | DO NOT UNBAN"), 38);
        return true;
    }
}