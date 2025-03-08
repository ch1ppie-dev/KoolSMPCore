package eu.koolfreedom.util;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.log.FLog;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class FUtil
{
    public static Map<String, ChatColor> CHAT_COLOR_NAMES;
    public static List<ChatColor> CHAT_COLOR_POOL;
    private static Random RANDOM;

    public static void cancel(BukkitTask task)
    {
        if (task == null)
        {
            return;
        }

        try
        {
            task.cancel();
        }
        catch (Exception ignored)
        {
        }
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
        Player player = Bukkit.getPlayer(sender.getName());
        String rank = KoolSMPCore.main.perms.getDisplay(player);
        String format = ChatColor.DARK_GRAY + "[ " + ChatColor.AQUA + " AC" + ChatColor.DARK_GRAY + "] " + ChatColor.BLUE + sender.getName() + ChatColor.DARK_GRAY + " [" + rank
                + ChatColor.DARK_GRAY + "] " + ChatColor.GOLD + message;
        Bukkit.getLogger().info(format);
        Bukkit.getOnlinePlayers()
                .stream()
                .filter((players) -> (players.hasPermission("kf.admin")))
                .forEachOrdered((players) -> players.sendMessage(format));
    }
}
