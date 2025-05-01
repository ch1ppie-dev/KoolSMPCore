package eu.koolfreedom.listener;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.util.FUtil;
import eu.koolfreedom.util.KoolSMPCoreBase;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
        event.motd(main.mmDeserialize(main.config.getString("server.motd"),
                TagResolver.resolver("randomize", FUtil.RandomColorTag.INSTANCE)));
    }
}
