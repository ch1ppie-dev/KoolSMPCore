package eu.koolfreedom.listener;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.util.FUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MuteManager implements Listener
{
    /** single source of truth */
    private final Set<UUID> muted = ConcurrentHashMap.newKeySet();
    private final Set<UUID> commandBlocked = ConcurrentHashMap.newKeySet();

    private final KoolSMPCore plugin;

    public MuteManager(KoolSMPCore plugin)
    {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /* -------------------------------------------------------------------- */
    /* Public API                                                           */
    /* -------------------------------------------------------------------- */

    public void mute(UUID uuid)
    {
        muted.add(uuid);
    }

    public void unmute(UUID uuid)
    {
        muted.remove(uuid);
    }

    public int getMuteCount() {
        return muted.size();
    }

    public boolean isMuted(UUID uuid)
    {
        return muted.contains(uuid);
    }

    public void setCommandsBlocked(UUID uuid, boolean block)
    {
        if (block) commandBlocked.add(uuid); else commandBlocked.remove(uuid);
    }

    public boolean isCommandsBlocked(UUID uuid) { return commandBlocked.contains(uuid); }

    public int wipeMutes()
    {
        int n = muted.size();
        muted.clear();
        return n;
    }

    public int wipeBlockedCommands()
    {
        int n = commandBlocked.size();
        commandBlocked.clear();
        return n;
    }

    /* Convenience overloads for Player / OfflinePlayer */
    public void mute(Player p){ mute(p.getUniqueId()); }
    public void unmute(Player p){ unmute(p.getUniqueId()); }
    public boolean isMuted(Player p){ return isMuted(p.getUniqueId()); }
    public boolean isMuted(OfflinePlayer p){ return isMuted(p.getUniqueId()); }

    /* -------------------------------------------------------------------- */
    /* Event listeners                                                      */
    /* -------------------------------------------------------------------- */

    @EventHandler
    public void onChat(AsyncChatEvent e)
    {
        if (isMuted(e.getPlayer()))
        {
            e.getPlayer().sendMessage(FUtil.miniMessage("<gray>You are muted."));
            e.setCancelled(true);
        }
    }

    // Legacy chat event
    @EventHandler @SuppressWarnings("deprecation")
    public void onLegacyChat(AsyncPlayerChatEvent e)
    {
        if (isMuted(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e)
    {
        UUID id = e.getPlayer().getUniqueId();

        if (isCommandsBlocked(id))
        {
            e.setCancelled(true);
            e.getPlayer().sendMessage(FUtil.miniMessage("<red>Your commands are blocked."));
            return;
        }

        if (isMuted(id))
        {
            e.setCancelled(true);
            e.getPlayer().sendMessage(FUtil.miniMessage("<red>You are muted, you cannot use commands."));
        }
    }

    /* keep mute after relog */
    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        if (isMuted(e.getPlayer()))
        {
            e.getPlayer().sendMessage(FUtil.miniMessage("<gray>You are still muted."));
        }
    }
}
