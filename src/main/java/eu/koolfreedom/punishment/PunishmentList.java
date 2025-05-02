package eu.koolfreedom.punishment;

import eu.koolfreedom.util.FUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PunishmentList
{
    private static Punishments punishments = Punishments.getConfig();

    private static SimpleDateFormat ISSUED = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    public static void logPunishment(OfflinePlayer punished, PunishmentType type, CommandSender punisher, String reason)
    {
        // TODO: Rewrite this too
        long id = System.currentTimeMillis();
        punishments.set(id + ".uuid", punished.getUniqueId().toString());
        if (punished.getName() != null)
            punishments.set(id + ".name", punished.getName());
        punishments.set(id + ".type", type.name());
        if (punished instanceof Player online)
            punishments.set(id + ".ip", FUtil.getIp(online));
        punishments.set(id + ".punisher", punisher.getName());
        punishments.set(id + ".reason", reason);
        punishments.set(id + ".issued", ISSUED.format(new Date()));
        punishments.save();
    }
}
