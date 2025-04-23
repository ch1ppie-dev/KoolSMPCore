package eu.koolfreedom.banning;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.config.ConfigEntry;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.text.SimpleDateFormat;

@SuppressWarnings("deprecation")
public class BanListener implements Listener
{
    private KoolSMPCore plugin;
    public BanListener(KoolSMPCore plugin)
    {
        this.plugin = plugin;
    }

    private static Bans bans = Bans.getConfig();

    private static PermBans permbans = PermBans.getConfig();

    private static SimpleDateFormat releaseTime = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e)
    {
        Player player = e.getPlayer();
        if (permbans.getStringList("names").contains(player.getName()) || permbans.getStringList("ips").contains(e.getAddress().getHostAddress().trim()))
        {
            StringBuilder permMsg = new StringBuilder()
                    .append(ChatColor.GOLD)
                    .append("You are " + ChatColor.RED + ChatColor.BOLD + " permanently " + ChatColor.GOLD + " banned from this server.")
                    .append("\nAppeal at: ")
                    .append(ChatColor.RED + ConfigEntry.SERVER_WEBSITE_OR_FORUM.getString());
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, permMsg.toString());
            return;
        }
        if (BanList.isBanned(player))
        {
            if (System.currentTimeMillis() >= bans.getLong(player.getName().toLowerCase() + ".length"))
            {
                BanList.removeBan(player);
                return;
            }
            StringBuilder banMsg = new StringBuilder()
                    .append(ChatColor.RED)
                    .append("You are currently banned from this server.")
                    .append("\nBanned by: ")
                    .append(bans.getString(player.getName().toLowerCase() + ".punisher"));
            if (bans.contains(player.getName().toLowerCase() + ".reason"))
            {
                banMsg.append("\nReason: ")
                        .append(bans.getString(player.getName().toLowerCase() + ".reason"));
            }
            banMsg.append("\nRelease: ")
                    .append(releaseTime.format(bans.getLong(player.getName().toLowerCase() + ".length")));
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, banMsg.toString());
        }
    }
}
