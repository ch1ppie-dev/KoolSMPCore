package eu.koolfreedom.listener;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.util.FUtil;
import eu.koolfreedom.util.KoolSMPCoreBase;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerListener extends KoolSMPCoreBase implements Listener
{
    public ServerListener(KoolSMPCore plugin)
    {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onServerPing(final ServerListPingEvent event)
    {
        event.motd(FUtil.miniMessage(ConfigEntry.SERVER_MOTD.getString(),
                TagResolver.resolver("randomize", FUtil.RandomColorTag.INSTANCE)));
    }
}
