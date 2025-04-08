package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.command.impl.Messages;
import eu.koolfreedom.discord.DiscordLogger;
import eu.koolfreedom.discord.StaffActionType;
import eu.koolfreedom.util.FUtil;
import eu.koolfreedom.util.StaffActionLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class DoomCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args)
    {
        if (!sender.hasPermission("kf.admin")) {
            sender.sendMessage(Messages.MSG_NO_PERMS);
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /" + s + " <player> [reason]", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND);
            return true;
        }

        FUtil.adminAction(sender.getName(), "Swinging the Russian Hammer over " + target.getName(), true);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **Swinging the Russian Hammer over " + target.getName() + "**");
        FUtil.bcastMsg(target.getName() + " will be squished flat", ChatColor.RED);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **" + target.getName() + " will be squished flat**");

        FUtil.adminAction(sender.getName(), "Removing " + target.getName() + " from the staff list", true);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **Removing " + target.getName() + " from the staff list**");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " clear");

        // Remove from whitelist
        target.setWhitelisted(false);

        // De-op (if already op'd)
        target.setOp(false);

        // Set gamemode to survival (if not in survival)
        target.setGameMode(GameMode.SURVIVAL);

        // Ignite player
        target.setFireTicks(10000);

        // Explosions
        target.getWorld().createExplosion(target.getLocation(), 0F, false);

        new BukkitRunnable() {
            @Override
            public void run()
            {
                // strike lightning
                target.getWorld().strikeLightningEffect(target.getLocation());

                // kill if not killed already
                target.setHealth(0.0);
            }
        }.runTaskLater(KoolSMPCore.main, 2L * 20L);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                // Add ban
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "koolsmpcore:ban " + target.getName() + " May your worst nightmare come true, and may you suffer by the hands of your ruler.");

                // more explosion
                target.getWorld().createExplosion(target.getLocation(), 0F, false);

                // Log ban
                DiscordLogger.sendStaffAction(StaffActionType.BAN, sender.getName(), target.getName(), "May your worst nightmare come true, and may you suffer by the hands of your ruler.");
                StaffActionLogger.log(StaffActionType.BAN, sender.getName(), target.getName(), "May your worst nightmare come true, and may you suffer by the hands of your ruler.");
            }
        }.runTaskLater(KoolSMPCore.main, 3L * 20L);
        return true;
    }
}
