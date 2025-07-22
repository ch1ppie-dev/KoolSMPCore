package eu.koolfreedom.api;

import eu.koolfreedom.util.FLog;
import org.bukkit.event.Listener;

import eu.koolfreedom.KoolSMPCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Set;
import java.util.UUID;

public class AltListener implements Listener
{
    private final AltManager manager = KoolSMPCore.getInstance().getAltManager();

    public AltListener()
    {
        Bukkit.getPluginManager().registerEvents(this, KoolSMPCore.getInstance());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        Player p = e.getPlayer();
        String ip = p.getAddress().getAddress().getHostAddress();

        manager.record(ip, p.getUniqueId());
        Set<UUID> alts = manager.getAlts(ip);
        if (alts.size() > 1)
        {
            Component msg = Component.text("⚠ ")
                    .append(Component.text(p.getName(), NamedTextColor.RED))
                    .append(Component.text(" shares an IP with: "))
                    .append(Component.text(alts.size() - 1 + " other account(s).", NamedTextColor.YELLOW));
            Bukkit.getOnlinePlayers().stream()
                    .filter(pl -> pl.hasPermission("kfc.admin"))
                    .forEach(pl -> pl.sendMessage(msg));
            FLog.info(msg);
        }
    }
}
