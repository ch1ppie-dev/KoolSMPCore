package eu.koolfreedom.stats;

import eu.koolfreedom.KoolSMPCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
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

    private final File file = new File(KoolSMPCore.getInstance().getDataFolder(), "playtime.yml");
    private final YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

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

    public void load()
    {
        if (!file.exists()) return;

        for (String key : config.getKeys(false))
        {
            UUID id = UUID.fromString(key);
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) continue;

            PlaytimeData data = new PlaytimeData();
            data.firstJoin = section.getLong("firstJoin", 0);
            data.lastSeen = section.getLong("lastSeen", 0);
            data.totalPlaySeconds = section.getLong("totalPlaySeconds", 0);
            this.data.put(id, data);
        }
    }

    public void save()
    {
        for (Map.Entry<UUID, PlaytimeData> entry : data.entrySet())
        {
            UUID id = entry.getKey();
            PlaytimeData d = entry.getValue();
            String path = id.toString();

            config.set(path + ".firstJoin", d.firstJoin);
            config.set(path + ".lastSeen", d.lastSeen);
            config.set(path + ".totalPlaySeconds", d.totalPlaySeconds);
        }

        try
        {
            config.save(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


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

    public void replace(UUID id, PlaytimeData newData) {
        data.put(id, newData);        // simple overwrite helper
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
            long now = Instant.now().getEpochSecond();
            long delta = now - startSecs;
            get(id).lastSeen = now * 1000;  // Also update last seen here
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
