package eu.koolfreedom.command;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.util.FUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
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

        if (!commandSender.hasPermission("kf.senior")) {
            commandSender.sendMessage(Messages.MSG_NO_PERMS);
            return false;
        }

        FUtil.adminAction(commandSender.getName(), "Unleashing Majora's Wrath over " + target.getName(), true);
        FUtil.bcastMsg(target.getName() + " will never see the light of day", ChatColor.RED);

        FUtil.adminAction(commandSender.getName(), "Removing " + target.getName() + " from the staff list", true);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " clear");

        // Remove from whitelist
        target.setWhitelisted(false);

        // De-op
        target.setOp(false);


        // Set gamemode to survival
        target.setGameMode(GameMode.SURVIVAL);

        // Ignite player
        target.setFireTicks(10000);

        // Explosions
        target.getWorld().createExplosion(target.getLocation(), 0F, false);

        // crash
        for (int i = 0; i < 3; i++) {
            Bukkit.getScheduler().runTaskLater(KoolSMPCore.main, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute as \"" + target.getName() + "\" at @s run particle flame ~ ~ ~ 1 1 1 1 999999999 force @s"), 30);
        }

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
                // discord message
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **" + commandSender.getName() + " - Obliterating " + target.getName() + "**");

                // more explosion
                target.getWorld().createExplosion(target.getLocation(), 0F, false);

                // add ban
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "banip " + target.getName() + " &eYou've met with a terrible fate haven't you? ");
            }
        }.runTaskLater(KoolSMPCore.main, 3L * 20L);
        return true;
    }
}
