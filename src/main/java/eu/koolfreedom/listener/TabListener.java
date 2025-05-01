package eu.koolfreedom.listener;


import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.util.KoolSMPCoreBase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TabListener extends KoolSMPCoreBase implements Listener
{
    public TabListener(KoolSMPCore plugin)
    {
        this.main = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        player.playerListName(main.perms.getColoredName(player));
    }
}
