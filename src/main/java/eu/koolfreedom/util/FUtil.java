package eu.koolfreedom.util;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.log.FLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.*;

@SuppressWarnings("deprecation")
public class FUtil // the f stands for fuck
{
    public static List<ChatColor> CHAT_COLOR_POOL;
    private static Random RANDOM;

    static
    {
        RANDOM = new Random();
        CHAT_COLOR_POOL = Arrays.stream(ChatColor.values()).toList();
    }

    @SuppressWarnings("deprecation")
    public static void adminAction(String adminName, String action, boolean isRed)
    {
        FUtil.bcastMsg(adminName + " - " + action, (isRed ? ChatColor.RED : ChatColor.AQUA));
    }

    @SuppressWarnings("deprecation")
    public static void bcastMsg(String message, ChatColor color)
    {
        bcastMsg(message, color, true);
    }

    @SuppressWarnings("deprecation")
    public static void bcastMsg(String message, ChatColor color, Boolean toConsole)
    {
        if (toConsole)
        {
            FLog.info(message);
        }

        for (Player player : Bukkit.getOnlinePlayers())
        {
            player.sendMessage((color == null ? "" : color) + message);
        }
    }

    public static void bcastMsg(String message, Boolean toConsole)
    {
        bcastMsg(message, null, toConsole);
    }

    public static void bcastMsg(String message)
    {
        FUtil.bcastMsg(message, null, true);
    }

    public static String colorize(final String string)
    {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static ChatColor randomChatColor()
    {
        return CHAT_COLOR_POOL.get(RANDOM.nextInt(CHAT_COLOR_POOL.size()));
    }

    public static void adminChat(CommandSender sender, String message)
    {
        Component formattedMessage = KoolSMPCore.main.mmDeserialize(ConfigEntry.FORMATS_ADMIN_CHAT.getString(),
                Placeholder.unparsed("name", sender.getName()),
                Placeholder.component("rank", KoolSMPCore.main.perms.getSenderGroup(sender).getColoredName()),
                Placeholder.unparsed("message", message));

        FLog.info(formattedMessage);

        Bukkit.getOnlinePlayers()
                .stream()
                .filter((players) -> (players.hasPermission("kf.admin")))
                .forEachOrdered((players) -> players.sendMessage(formattedMessage));
    }

    public static File getPluginFile(Plugin plugin, String name)
    {
        return new File(plugin.getDataFolder(), name);
    }

    public static String getIp(Player player)
    {
        return player.getAddress().getAddress().getHostAddress().trim();
    }
}
