package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.banning.Ban;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.util.FUtil;
import eu.koolfreedom.punishment.PunishmentList;
import eu.koolfreedom.punishment.PunishmentType;
import eu.koolfreedom.banning.BanType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BanCommand extends KoolCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0)
        {
            return false;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.isOnline() && !target.hasPlayedBefore())
        {
            msg(sender, playerNotFound);
            return true;
        }

        final Ban ban = Ban.fromPlayer(target, sender.getName(), args.length > 1 ? String.join(" ", ArrayUtils.remove(args, 0)) : null, BanType.BAN);
        KoolSMPCore.getPlugin().bm.addBan(ban);
        FUtil.staffAction(sender, "Banned <player>", Placeholder.unparsed("player", target.getName() != null ? target.getName() : args[0]));

        if (target instanceof Player online)
        {
            PunishmentList.logPunishment(online, PunishmentType.BAN, sender, ban.getReason());

            // Now for the fun part...
            for (int i = 0; i < 4; i++)
            {
                online.getWorld().strikeLightning(online.getLocation());
            }
            online.setHealth(0);

            // We had our fun, they're gone
            online.kick(ban.getKickMessage());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String commandLabel, String[] args)
    {
        return args.length == 1 ? Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(name -> !name.equalsIgnoreCase(sender.getName())).toList() : List.of();
    }
}
