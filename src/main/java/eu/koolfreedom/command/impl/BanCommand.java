package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.util.FUtil;
import eu.koolfreedom.punishment.PunishmentList;
import eu.koolfreedom.punishment.PunishmentType;
import eu.koolfreedom.banning.BanType;
import eu.koolfreedom.banning.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.apache.commons.lang.StringUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BanCommand implements CommandExecutor
{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String c, String[] args)
    {
        if (!sender.hasPermission("kf.admin"))
        {
            sender.sendMessage(Messages.MSG_NO_PERMS);
        }

        if (args.length == 0)
        {
            return false;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null)
        {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND);
            return true;
        }

        StringBuilder kickMsg = new StringBuilder()
                .append(ChatColor.RED)
                .append("You have been banned from this server.")
                .append("\nBanned by: ")
                .append(sender.getName());

        String reason = null;
        if (args.length > 1)
        {
            reason = StringUtils.join(args, " ", 1, args.length);
            kickMsg.append("\nReason: ")
                    .append(reason);
        }

        // shoot the player up
        player.setAllowFlight(true);
        player.setFlying(false);
        player.getWorld().createExplosion(player.getLocation(), 0.5F);
        player.setVelocity(player.getVelocity().clone().add(new Vector(0, 20, 0)));

        // add ban
        BanList.addBan(player, sender, BanType.BAN, reason);

        // log ban
        PunishmentList.logPunishment(player, PunishmentType.BAN, sender, reason);

        new BukkitRunnable()
        {
            public void run()
            {
                // announce
                FUtil.adminAction(sender.getName(), "Banning " + player.getName(), true);

                // de-op
                player.setOp(false);

                // set to survival
                player.setGameMode(GameMode.SURVIVAL);

                // clear inventory
                player.getInventory().clear();

                // strike 4 lightning bolts
                for (int i = 0; i < 4; i++)
                {
                    player.getWorld().strikeLightning(player.getLocation());
                }

                // large explosion
                player.getWorld().createExplosion(player.getLocation(), 8F);

                // kill
                player.setHealth(0.0);

                // kick player
                player.kickPlayer(kickMsg.toString());
            }
        }.runTaskLater(KoolSMPCore.main, 2L * 20L);
        return true;
    }
}
