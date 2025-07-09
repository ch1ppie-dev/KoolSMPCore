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

        // cancel any previous timer
        BukkitTask old = muteUndoTasks.remove(uuid);
        if (old != null) old.cancel();

        BukkitTask task = new BukkitRunnable()
        {
            @Override
            public void run() {
                // fetch managers fresh – in case they were (re)loaded later
                MuteManager mm = KoolSMPCore.getInstance().getMuteManager();
                if (mm == null) {            // should not happen, but be safe
                    cancel();
                    return;
                }

                if (mm.isMuted(uuid))
                {
                    mm.unmute(uuid);

                    // broadcast & tell player (if online)
                    Player online = Bukkit.getPlayer(uuid);
                    FUtil.staffAction(
                            Bukkit.getConsoleSender(),
                            "Auto‑unmuted <player>",
                            Placeholder.unparsed("player", online != null ? online.getName() : uuid.toString())
                    );
                    if (online != null)
                        online.sendMessage(FUtil.miniMessage("<green>Your mute has been automatically lifted after 5 minutes."));
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

    public void scheduleAutoUnfreeze(Player player) {
        UUID uuid = player.getUniqueId();

        // Cancel an existing task if one exists
        BukkitTask existing = freezeUndoTasks.remove(uuid);
        if (existing != null) existing.cancel();

        // Schedule a new task
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                // Get player safely
                Player p = Bukkit.getPlayer(uuid);
                if (p == null || !p.isOnline()) {
                    FLog.error("[DEBUG] Auto-unfreeze: player is offline or null: " + uuid);
                    freezeUndoTasks.remove(uuid);
                    return;
                }

                if (freezeManager.isFrozen(p)) {
                    FLog.error("[DEBUG] Auto-unfreeze: unfreezing " + p.getName());

                    // Message and broadcast first
                    FUtil.staffAction(Bukkit.getConsoleSender(), "Auto-unfroze <player>",
                            Placeholder.unparsed("player", p.getName()));
                    p.sendMessage(FUtil.miniMessage("<green>Your freeze has been automatically lifted after 5 minutes."));

                    // Then unfreeze
                    freezeManager.unfreeze(player);
                } else {
                    FLog.error("[DEBUG] Auto-unfreeze: player " + p.getName() + " is not frozen.");
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
