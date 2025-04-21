package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.banning.BanList;
import eu.koolfreedom.banning.BanType;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.punishment.PunishmentList;
import eu.koolfreedom.punishment.PunishmentType;
import eu.koolfreedom.util.FUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class DoomCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args)
    {
        if (!sender.hasPermission("kf.admin")) {
            sender.sendMessage(Messages.MSG_NO_PERMS);
            return false;
        }

        if (args.length != 1)
        {
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null)
        {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND);
            return true;
        }

        FUtil.adminAction(sender.getName(), "Swinging the Russian Hammer over " + target.getName(), true);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **" + sender.getName() + " - Swinging the Russian Hammer over " + target.getName() + "**");


        FUtil.bcastMsg(target.getName() + " will be squished flat", ChatColor.RED);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **" + target.getName() + " will be squished flat**");

        // shoot the player up
        target.setAllowFlight(true);
        target.setFlying(false);
        target.getWorld().createExplosion(target.getLocation(), 5F);
        target.setVelocity(target.getVelocity().clone().add(new Vector(0, 25, 0)));

        // log doom
        PunishmentList.logPunishment(target, PunishmentType.DOOM, sender, "May your worst nightmare come true, and may you suffer by the hands of your ruler.");

        new BukkitRunnable()
        {
            public void run()
            {
                // announce
                FUtil.adminAction(sender.getName(), "Banning " + target.getName(), true);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **" + sender.getName() + " - Banning " + target.getName() + "**");

                // remove rank
                FUtil.adminAction(sender.getName(), "Removing " + target.getName() + " from the staff list", true);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **" + sender.getName() + " - Removing " + target.getName() + " from the staff list**");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " clear");

                // add ban
                BanList.addBan(target, sender, BanType.BAN, "May your worst nightmare come true, and may you suffer by the hands of your ruler.");

                // de-op
                target.setOp(false);

                // set to survival
                target.setGameMode(GameMode.SURVIVAL);

                // strike 4 lightning bolts
                for (int i = 0; i < 50; i++)
                {
                    target.getWorld().strikeLightning(target.getLocation());
                }

                // large explosion
                target.getWorld().createExplosion(target.getLocation(), 100F);

                // kill
                target.setHealth(0.0);

                // kick player
                target.kickPlayer(ChatColor.RED + "May your worst nightmare come true, and may you suffer by the hands of your ruler.");
            }
        }.runTaskLater(KoolSMPCore.main, 2L * 20L);
        return true;
    }
}
