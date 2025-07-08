package eu.koolfreedom.combat;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.banning.Ban;
import eu.koolfreedom.util.FUtil;
import eu.koolfreedom.util.TimeOffset;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very small combat‑logger helper that respects WorldGuard <code>PVP=DENY</code>
 * regions ("safe‑zones").
 *
 * <p>Logic:</p>
 * <ul>
 *   <li>Whenever two players inflict damage on each other <b>outside</b> a
 *       safe‑zone, both are tagged for {@link #COMBAT_TAG_SECONDS} seconds.</li>
 *   <li>If a tagged player logs out before the timer expires, we instantly
 *       temp‑ban them for 24 hours.</li>
 *   <li>Tags are cleared automatically on player death or when the timer
 *       elapses.</li>
 *   <li>Players with permission <code>kfc.combatlog.bypass</code> or creative‑mode
 *       are ignored.</li>
 * </ul>
 */
public class CombatManager implements Listener
{

    /* ──────────────────────────────────────────────────────────────────── */
    /*  Constants                                                          */
    /* ──────────────────────────────────────────────────────────────────── */

    /** Time (sec) a player stays in combat after the last hit. */
    public static final int COMBAT_TAG_SECONDS = 15;

    // Tracks active action bar countdowns
    private final Set<UUID> actionBarSet = ConcurrentHashMap.newKeySet();


    /* ──────────────────────────────────────────────────────────────────── */
    /*  State                                                              */
    /* ──────────────────────────────────────────────────────────────────── */

    /** uuid → epoch‑ms when the tag expires */
    private final Map<UUID, Long> tagMap = new ConcurrentHashMap<>();

    private final KoolSMPCore plugin;

    public CombatManager(KoolSMPCore plugin)
    {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // House‑keeping task: runs every second, removes expired tags
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            tagMap.entrySet().removeIf(e -> e.getValue() <= now);
        }, 20L, 20L);
    }

    /* ──────────────────────────────────────────────────────────────────── */
    /*  Combat tagging                                                     */
    /* ──────────────────────────────────────────────────────────────────── */

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent e)
    {
        if (!(e.getEntity()   instanceof Player victim))   return;
        if (!(e.getDamager() instanceof Player attacker)) return;

        // bypass permission or creative‑mode ‑> ignore
        if (canBypass(attacker) || canBypass(victim)) return;

        // WorldGuard safe‑zone? do nothing
        if (isInSafeZone(attacker) || isInSafeZone(victim)) return;

        /* Tag both players */
        tag(attacker);
        tag(victim);
    }

    /** Clear tag on death so players aren't punished if they die. */
    @EventHandler
    public void onDeath(PlayerDeathEvent e)
    {
        untag(e.getEntity().getUniqueId());
    }

    /** Punish logging out while tagged. */
    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        UUID id = e.getPlayer().getUniqueId();
        if (isTagged(id))
        {
            punish(e.getPlayer());
            untag(id);
        }
    }

    /* ──────────────────────────────────────────────────────────────────── */
    /*  Public helpers                                                     */
    /* ──────────────────────────────────────────────────────────────────── */

    /** Mark player as in‑combat. */
    public void tag(Player p) {
        UUID uuid = p.getUniqueId();
        long expireAt = System.currentTimeMillis() + COMBAT_TAG_SECONDS * 1000L;
        tagMap.put(uuid, expireAt);

        p.sendMessage(Component.text("You are now in combat! Logging out will punish you.")
                .color(NamedTextColor.RED));

        if (actionBarSet.add(uuid)) {
            Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                if (!isTagged(uuid)) {
                    actionBarSet.remove(uuid);
                    task.cancel();
                    return;
                }

                long remaining = (tagMap.get(uuid) - System.currentTimeMillis()) / 1000;
                if (remaining < 0) remaining = 0;

                Player online = Bukkit.getPlayer(uuid);
                if (online != null && online.isOnline()) {
                    online.sendActionBar(Component.text("In Combat: " + remaining + "s")
                            .color(NamedTextColor.RED));
                }
            }, 0L, 20L); // every second
        }
    }

    /** Remove combat tag manually. */
    public void untag(UUID uuid)
    {
        tagMap.remove(uuid);
        actionBarSet.remove(uuid);
    }

    /** Whether player is currently tagged. */
    public boolean isTagged(UUID uuid)
    {
        Long until = tagMap.get(uuid);
        return until != null && until > System.currentTimeMillis();
    }

    /* ──────────────────────────────────────────────────────────────────── */
    /*  Punishment logic                                                   */
    /* ──────────────────────────────────────────────────────────────────── */

    private void punish(Player p)
    {
        // Kill the player so items drop, and death screen appears on relog
        p.setHealth(0.0);

        // 24‑hour temp‑ban
        Ban ban = Ban.fromPlayer(p, "CombatLogger", "Logged out during combat | 24 hour auto-ban", TimeOffset.getOffset("1d"));
        plugin.getBanManager().addBan(ban);

        FUtil.broadcast("<red>[CombatLog] <dark_red><player> has logged out during combat. Auto-banned for 24 hours", Placeholder.unparsed("player", p.getName()));
    }

    /* ──────────────────────────────────────────────────────────────────── */
    /*  Utility & checks                                                   */
    /* ──────────────────────────────────────────────────────────────────── */

    private boolean canBypass(Player p)
    {
        return p.hasPermission("kfc.combatlog.bypass") || p.getGameMode() == GameMode.CREATIVE;
    }

    /** Returns true if the location is covered by a WG region with PVP=DENY. */
    private boolean isInSafeZone(Player player)
    {
        World w = player.getWorld();
        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w));
        if (rm == null) return false;

        ApplicableRegionSet set = rm.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));
        // testState == true means the flag ALLOWS the action. We want the opposite.
        return !set.testState(null, Flags.PVP);
    }
}
