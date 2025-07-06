package eu.koolfreedom.stats;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Extremely lightweight, in‑memory play‑time tracker.
 */
public class PlaytimeManager
{
    /* -------------------------------------------------------------------- */
    /*  Inner data‑holder                                                   */
    /* -------------------------------------------------------------------- */

    public static class PlaytimeData
    {
        /** Epoch‑ms of very first join. 0 = unknown / not recorded yet. */
        public long firstJoin        = 0;

        /** Epoch‑ms of the last time the player was seen online.        */
        public long lastSeen         = 0;

        /** Total seconds played across all sessions.                    */
        public long totalPlaySeconds = 0;
    }

    /* -------------------------------------------------------------------- */
    /*  State                                                                */
    /* -------------------------------------------------------------------- */

    private final Map<UUID, PlaytimeData> data         = new HashMap<>();
    private final Map<UUID, Long>         sessionStart = new HashMap<>();

    /* -------------------------------------------------------------------- */
    /*  Basic CRUD                                                          */
    /* -------------------------------------------------------------------- */

    /** Get (or create) the play‑time record for a player. */
    public PlaytimeData get(UUID id)
    {
        return data.computeIfAbsent(id, k -> new PlaytimeData());
    }

    /** Replace the stored record (used by /seen after Essentials import). */
    public void set(UUID id, PlaytimeData pd)
    {
        data.put(id, pd);
    }

    /* -------------------------------------------------------------------- */
    /*  Event hooks                                                         */
    /* -------------------------------------------------------------------- */

    /** Call this from PlayerJoinEvent. */
    public void handleJoin(UUID id)
    {
        PlaytimeData d = get(id);
        long now = Instant.now().toEpochMilli();   // store in ms for consistency with Bukkit API

        if (d.firstJoin == 0)              // first‑ever join
            d.firstJoin = now;

        d.lastSeen = now;
        sessionStart.put(id, now / 1000);  // store seconds for delta calc
    }

    /** Call this from PlayerQuitEvent. */
    public void handleQuit(UUID id)
    {
        Long startSecs = sessionStart.remove(id);
        if (startSecs != null)
        {
            long delta = Instant.now().getEpochSecond() - startSecs;
            get(id).totalPlaySeconds += Math.max(delta, 0);
        }
    }

    /* -------------------------------------------------------------------- */
    /*  Convenience                                                         */
    /* -------------------------------------------------------------------- */

    public long getPlaytime(UUID id)
    {
        return get(id).totalPlaySeconds;
    }
}
