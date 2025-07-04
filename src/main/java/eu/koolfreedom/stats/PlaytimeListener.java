package eu.koolfreedom.stats;

import eu.koolfreedom.KoolSMPCore;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlaytimeListener implements Listener
{
    private final PlaytimeManager mgr = KoolSMPCore.getInstance().getPlaytimeManager();

    public PlaytimeListener()
    {
        Bukkit.getPluginManager().registerEvents(this, KoolSMPCore.getInstance());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        mgr.handleJoin(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        mgr.handleQuit(e.getPlayer().getUniqueId());
    }
}
