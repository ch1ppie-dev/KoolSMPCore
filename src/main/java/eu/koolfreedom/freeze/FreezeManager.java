package eu.koolfreedom.freeze;

import eu.koolfreedom.KoolSMPCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;

public class FreezeManager
{
    private final KoolSMPCore plugin;
    private final Map<UUID, FreezeEntry> frozen = new HashMap<>();
    @Getter
    private boolean globalFreeze = false;
    private final long PURGE_SECONDS = 300; // default 5 minutes before auto-purge

    public FreezeManager(KoolSMPCore plugin)
    {
        this.plugin = plugin;
    }

    public void freeze(Player p, long seconds)
    {
        unfreeze(p); // if already frozen, reset

        p.setAllowFlight(true);
        p.setFlying(true);

        BukkitTask task = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                unfreeze(p);
            }
        }.runTaskLater(plugin, seconds * 20L);

        frozen.put(
                p.getUniqueId(),
                new FreezeEntry(p.getLocation(), task)
        );
    }

    public void freeze(Player p)
    {
        freeze(p, PURGE_SECONDS);
    }

    public void unfreeze(Player p)
    {
        FreezeEntry entry = frozen.remove(p.getUniqueId());
        if(entry != null && entry.task != null)
        {
            entry.task.cancel();
        }

        // restore flight only if they weren’t already in creative
        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            p.setFlying(false);
            p.setAllowFlight(false);
        }
    }

    public boolean isFrozen(Player p)
    {
        return frozen.containsKey(p.getUniqueId());
    }

    /* ---------- Global ---------- */

    public void setGlobalFreeze(boolean enabled) {
        globalFreeze = enabled;
        if (!enabled) {           // un‑freeze everyone
            Bukkit.getOnlinePlayers().forEach(this::unfreeze);
        } else {                  // freeze everyone that’s not op
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(pl -> !pl.isOp())
                    .forEach(this::freeze);
        }
    }

    /* =========  DATA CLASS  ========= */

    private record FreezeEntry(Location loc, BukkitTask task)
    {
        // nothing
    }
}
