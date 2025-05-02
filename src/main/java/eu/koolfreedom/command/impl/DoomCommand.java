package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.banning.Ban;
import eu.koolfreedom.banning.BanType;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.util.FUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.LuckPerms;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class DoomCommand extends KoolCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0)
        {
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null)
        {
            msg(sender, playerNotFound);
            return true;
        }

        String reason = args.length >= 2 ? String.join(" ", ArrayUtils.remove(args, 0)) : null;

        eviscerate(target, sender, reason);
        return true;
    }

    public static void eviscerate(Player target, CommandSender sender, String reason)
    {
        // Get the technical side of things out of the way first, we want this user banned first and foremost
        final Ban ban = Ban.fromPlayer(target, sender.getName(), reason != null ? reason : "You've met with a terrible fate, haven't you?", BanType.BAN);
        KoolSMPCore.getInstance().banManager.addBan(ban);

        // Strip them of their data in LuckPerms
        Objects.requireNonNull(KoolSMPCore.getLuckPermsAPI()).getUserManager().deletePlayerData(target.getUniqueId());
        if (KoolSMPCore.getLuckPermsAPI() != null)
        {
            LuckPerms luckPerms = KoolSMPCore.getLuckPermsAPI();

            luckPerms.getUserManager().deletePlayerData(target.getUniqueId());
            luckPerms.getUserManager().modifyUser(target.getUniqueId(), user ->
            {
                user.setPrimaryGroup("default");
                user.data().clear();
            });
        }

        // Stop them dead in their tracks
        target.spawnParticle(Particle.ASH, target.getLocation(), Integer.MAX_VALUE, 1, 1, 1, 1, null, true);

        // Now we can have our fun
        FUtil.broadcast("<red>Hey, <player>, what's the difference between jelly and jam?",
                Placeholder.unparsed("player", target.getName()));

        // ZAP
        target.setGameMode(GameMode.ADVENTURE);
        target.getWorld().strikeLightning(target.getLocation());
        target.getWorld().createExplosion(target.getLocation(), 0F, false);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                FUtil.broadcast("<red>I can't jelly my ban hammer down your throat.");

                if (!target.isOnline())
                {
                    FUtil.broadcast("<red>Not even running away will save you.");
                }

                target.getWorld().strikeLightning(target.getLocation());
                target.getWorld().createExplosion(target.getLocation(), 0F, false);
            }
        }.runTaskLater(KoolSMPCore.getInstance(), 20L);

        for (int i = 1; i < 4; i++)
        {
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    target.getWorld().strikeLightning(target.getLocation());
                    target.getWorld().createExplosion(target.getLocation(), 0F, false);
                }
            }.runTaskLater(KoolSMPCore.getInstance(), 30 * i);
        }

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (target.isOnline())
                {
                    target.kick(ban.getKickMessage());
                }

                target.getWorld().strikeLightning(target.getLocation());
                target.getWorld().createExplosion(target.getLocation(), 0F, false);

                FUtil.staffAction(sender, "Banned <player> with excessive violence",
                        Placeholder.unparsed("player", target.getName()));
            }
        }.runTaskLater(KoolSMPCore.getInstance(), 135L);
    }


}