package eu.koolfreedom.listener;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.util.FUtil;
import eu.koolfreedom.util.KoolSMPCoreBase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LoginListener extends KoolSMPCoreBase implements Listener
{
    public LoginListener(KoolSMPCore plugin)
    {
        this.main = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        String header = main.getConfig().getString("server.tablist_header");
        String footer = main.getConfig().getString("server.tablist_footer");
        if (!(header == null))
        {
            player.setPlayerListHeader(FUtil.colorize(header).replace("\\n", "\n"));
        }
        if (!(footer == null))
        {
            player.setPlayerListFooter(FUtil.colorize(footer).replace("\\n", "\n"));
        }
    }
}
