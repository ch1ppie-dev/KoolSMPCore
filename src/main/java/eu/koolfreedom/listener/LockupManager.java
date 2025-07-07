package eu.koolfreedom.listener;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LockupManager implements Listener
{
    private final Map<UUID, BukkitRunnable> locked = new ConcurrentHashMap<>();
    private final KoolSMPCore plugin;

    public LockupManager(KoolSMPCore plugin)
    {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /* --------------------------------------------------------------------- */
    /*  Public API                                                           */
    /* --------------------------------------------------------------------- */

    /** @return the new state (true = now locked, false = now unlocked) */
    public boolean toggle(Player p)
    {
        if (locked.containsKey(p.getUniqueId()))
        {
            unlock(p.getUniqueId());
            return false;
        }
        else
        {
            lock(p);
            return true;
        }
    }

    public boolean isLocked(UUID uuid)
    {
        return locked.containsKey(uuid);
    }

    /* --------------------------------------------------------------------- */
    /*  Implementation                                                       */
    /* --------------------------------------------------------------------- */

    private void lock(Player p)
    {
        // Keep inventory screen open every tick (prevents ESC)
        BukkitRunnable task = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!p.isOnline())
                {
                    cancel();
                    return;
                }
                if (p.getOpenInventory() == null)
                    p.openInventory(p.getInventory());
            }
        };
        task.runTaskTimer(plugin, 0L, 10L);          // every 0.5s
        locked.put(p.getUniqueId(), task);
    }

    private void unlock(UUID uuid)
    {
        BukkitRunnable task = locked.remove(uuid);
        if (task != null) task.cancel();
        Player p = Bukkit.getPlayer(uuid);
        if (p != null && p.isOnline())
            p.closeInventory();
    }

    /* --------------------------------------------------------------------- */
    /*  Event guards – stop locked players from moving or running commands   */
    /* --------------------------------------------------------------------- */

    @EventHandler
    public void onMove(PlayerMoveEvent e)
    {
        if (isLocked(e.getPlayer().getUniqueId()) && e.getFrom().getBlockX() != e.getTo().getBlockX())
            e.setTo(e.getFrom());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e)
    {
        if (isLocked(e.getPlayer().getUniqueId()))
        {
            e.setCancelled(true);
            e.getPlayer().sendMessage(FUtil.miniMessage("<red>You are locked‑up and cannot use commands."));
        }
    }
}
