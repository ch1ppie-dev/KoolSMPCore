package eu.koolfreedom.listener;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.util.FUtil;
import eu.koolfreedom.util.KoolSMPCoreBase;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerListener extends KoolSMPCoreBase implements Listener
{
    public ServerListener(KoolSMPCore plugin)
    {
        this.main = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onServerPing(final ServerListPingEvent event)
    {
        String baseMotd = main.getConfig().getString("server.motd");
        baseMotd = baseMotd.replace("\\n", "\n");
        baseMotd = FUtil.colorize(baseMotd);
        final StringBuilder motd = new StringBuilder();
        for (final String word : baseMotd.split(" "))
        {
            motd.append(FUtil.randomChatColor()).append(word).append(" ");
        }
        event.setMotd(motd.toString().trim());
    }
}
