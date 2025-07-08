package eu.koolfreedom.combat;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple combat‑logger helper that respects WorldGuard <code>PVP=DENY</code>
 * regions ("safe‑zones"). When a player is tagged, they remain "in combat" for
 * {@link #COMBAT_TAG_SECONDS}. Logging out while tagged should be punished by
 * a separate listener (kick/ban/kill etc.).
 */
public class CombatManager implements Listener {

    /* ────────────────────────────────────────────────────────────────────── */
    /*  Configuration                                                        */
    /* ────────────────────────────────────────────────────────────────────── */

    /** Time (sec) a player stays in combat after the last hit. */
    public static final int COMBAT_TAG_SECONDS = 15;

    /* ────────────────────────────────────────────────────────────────────── */
    /*  State                                                                */
    /* ────────────────────────────────────────────────────────────────────── */

    /** uuid → epoch‑ms when the tag expires */
    private final Map<UUID, Long> tagged = new ConcurrentHashMap<>();

    private final JavaPlugin plugin;

    public CombatManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // simple repeating task to un‑tag expired entries
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            tagged.entrySet().removeIf(e -> e.getValue() <= now);
        }, 20L, 20L); // run every second
    }

    /* ────────────────────────────────────────────────────────────────────── */
    /*  LISTENERS                                                            */
    /* ────────────────────────────────────────────────────────────────────── */

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent event) {
        if (!(event.getEntity()   instanceof Player victim))   return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        // Ignore combat that starts in a WorldGuard "safe‑zone"
        if (isInSafeZone(attacker) || isInSafeZone(victim)) return;

        // Tag both participants
        tag(attacker.getUniqueId());
        tag(victim.getUniqueId());
    }

    /* ────────────────────────────────────────────────────────────────────── */
    /*  Public API                                                           */
    /* ────────────────────────────────────────────────────────────────────── */

    /** Mark a player as "in combat" for {@link #COMBAT_TAG_SECONDS}. */
    public void tag(UUID uuid) {
        long expires = System.currentTimeMillis() + COMBAT_TAG_SECONDS * 1000L;
        tagged.put(uuid, expires);
    }

    /** Returns <code>true</code> if the player is currently tagged. */
    public boolean isTagged(UUID uuid) {
        Long until = tagged.get(uuid);
        return until != null && until > System.currentTimeMillis();
    }

    /* ────────────────────────────────────────────────────────────────────── */
    /*  UTILITIES                                                            */
    /* ────────────────────────────────────────────────────────────────────── */

    /**
     * Checks if the given player is standing in a region where the WG <code>PVP</code>
     * flag is explicitly <code>DENY</code> (i.e. combat‑safe area).
     */
    private boolean isInSafeZone(Player player) {
        World world   = player.getWorld();
        Location loc  = player.getLocation();

        RegionManager rm = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(world));
        if (rm == null) {
            return false;             // no regions defined in this world
        }

        ApplicableRegionSet set = rm.getApplicableRegions(BukkitAdapter.asBlockVector(loc));

        // WG 7.x: testState returns TRUE if the flag ALLOWS the action.
        // We want a safe‑zone when PVP is NOT allowed.
        boolean pvpAllowed = set.testState(null, Flags.PVP);
        return !pvpAllowed;           // safe‑zone if PVP is denied
    }
}
