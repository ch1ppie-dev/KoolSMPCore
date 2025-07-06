package eu.koolfreedom.util;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.freeze.FreezeManager;
import eu.koolfreedom.listener.MuteManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AutoUndoManager
{
    private final KoolSMPCore plugin;
    private final MuteManager muteManager;
    private final FreezeManager freezeManager;

    // Tasks for auto unmute/unfreeze by player UUID
    private final Map<UUID, BukkitTask> muteUndoTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> freezeUndoTasks = new ConcurrentHashMap<>();

    private static final long AUTO_UNDO_TICKS = 5 * 60 * 20L; // 5 minutes in ticks

    public AutoUndoManager(KoolSMPCore plugin, MuteManager muteManager, FreezeManager freezeManager)
    {
        this.plugin = plugin;
        this.muteManager = muteManager;
        this.freezeManager = freezeManager;
    }

    /* ------------------------- */
    /* Mute auto-undo */
    /* ------------------------- */

    public void scheduleAutoUnmute(Player player)
    {
        UUID uuid = player.getUniqueId();

        // Cancel existing task if present
        BukkitTask existing = muteUndoTasks.remove(uuid);
        if (existing != null) existing.cancel();

        // Schedule new task
        BukkitTask task = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (muteManager.isMuted(uuid))
                {
                    muteManager.unmute(uuid);
                    FUtil.staffAction(Bukkit.getConsoleSender(), "Auto-unmuted <player> after 5 minutes", Placeholder.unparsed("player", player.getName()));
                    player.sendMessage(FUtil.miniMessage("<green>Your mute has been automatically lifted after 5 minutes."));
                }
                muteUndoTasks.remove(uuid);
            }
        }.runTaskLater(plugin, AUTO_UNDO_TICKS);

        muteUndoTasks.put(uuid, task);
    }

    public void cancelAutoUnmute(UUID uuid)
    {
        BukkitTask task = muteUndoTasks.remove(uuid);
        if (task != null) task.cancel();
    }

    /* ------------------------- */
    /* Freeze auto-undo */
    /* ------------------------- */

    public void scheduleAutoUnfreeze(Player player)
    {
        UUID uuid = player.getUniqueId();

        BukkitTask existing = freezeUndoTasks.remove(uuid);
        if (existing != null) existing.cancel();

        BukkitTask task = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (freezeManager.isFrozen(player))
                {
                    freezeManager.unfreeze(player);
                    FUtil.staffAction(Bukkit.getConsoleSender(), "Auto-unfroze <player>", Placeholder.unparsed("player", player.getName()));
                    player.sendMessage(FUtil.miniMessage("<green>Your freeze has been automatically lifted after 5 minutes."));
                }
                freezeUndoTasks.remove(uuid);
            }
        }.runTaskLater(plugin, AUTO_UNDO_TICKS);

        freezeUndoTasks.put(uuid, task);
    }

    public void cancelAutoUnfreeze(UUID uuid)
    {
        BukkitTask task = freezeUndoTasks.remove(uuid);
        if (task != null) task.cancel();
    }
}
