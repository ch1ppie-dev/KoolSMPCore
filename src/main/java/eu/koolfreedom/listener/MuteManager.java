package eu.koolfreedom.listener;

import eu.koolfreedom.KoolSMPCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

public class MuteManager implements Listener
{
    private KoolSMPCore plugin;
    public MuteManager(KoolSMPCore plugin)
    {
        this.plugin = plugin;
    }

    private static List<Player> muted = new ArrayList<>();

    public static int getMutedAmount()
    {
        return muted.size();
    }

    public static void setMuted(Player player, boolean mute)
    {
        if (mute)
        {
            muted.add(player);
            return;
        }
        muted.remove(player);
    }

    public static void wipeMutes()
    {
        muted.clear();
    }

    public static boolean isMuted(Player player)
    {
        return muted.contains(player);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e)
    {
        Player player = e.getPlayer();
        if (isMuted(player))
        {
            e.setCancelled(true);
            player.sendMessage(ChatColor.GRAY + "You are currently muted and cannot chat.");
        }
    }
}