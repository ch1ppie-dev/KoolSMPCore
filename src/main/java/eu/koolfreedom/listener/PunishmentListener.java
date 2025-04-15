package eu.koolfreedom.listener;

import eu.koolfreedom.config.ConfigEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class PunishmentListener implements Listener
{
    // Ban
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        if (Bukkit.getBanList(BanList.Type.NAME).isBanned(event.getPlayer().getName()))
        {
            String reason = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(event.getPlayer().getName()).getReason();
            String source = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(event.getPlayer().getName()).getSource();
            String appeal = ConfigEntry.SERVER_WEBSITE_OR_FORUM.getString();
            {
                StringBuilder kick = new StringBuilder()
                        .append(ChatColor.GOLD)
                        .append("You've been banned from this server")
                        .append("\nBanned by: ")
                        .append(ChatColor.RED)
                        .append(source)
                        .append(ChatColor.GOLD)
                        .append("\nReason: ")
                        .append(ChatColor.RED)
                        .append(reason)
                        .append(ChatColor.GOLD)
                        .append("\nYou may appeal your ban at: ")
                        .append(ChatColor.RED)
                        .append(appeal);
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kick.toString());
            }
        }
    }

    // Mute
    private static List<Player> mutelist = new ArrayList<>();

    public static boolean isMuted(Player player)
    {
        return mutelist.contains(player);
    }

    public static void addMute(Player player)
    {
        mutelist.add(player);
    }

    public static void removeMute(Player player)
    {
        mutelist.remove(player);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();
        if (isMuted(player))
        {
            player.sendMessage(ChatColor.DARK_RED + "You have been muted, you may not talk");
            event.setCancelled(true);
        }
    }
}

