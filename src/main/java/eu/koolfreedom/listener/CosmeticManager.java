package eu.koolfreedom.listener;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class CosmeticManager implements Listener
{
    public CosmeticManager()
    {
        Bukkit.getServer().getPluginManager().registerEvents(this, KoolSMPCore.getInstance());
    }

    @EventHandler
    public void onServerPing(final ServerListPingEvent event)
    {
        event.motd(FUtil.miniMessage(ConfigEntry.SERVER_MOTD.getString()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        String header = ConfigEntry.SERVER_TABLIST_HEADER.getString();
        String footer = ConfigEntry.SERVER_TABLIST_FOOTER.getString();

        if (header != null)
        {
            player.sendPlayerListHeader(FUtil.miniMessage(header));
        }

        if (footer != null)
        {
            player.sendPlayerListFooter(FUtil.miniMessage(footer));
        }

        player.playerListName(KoolSMPCore.getInstance().getGroupManager().getColoredName(player));

        KoolSMPCore.getInstance().getGroupManager().applyNametagColor(player);
    }
}
