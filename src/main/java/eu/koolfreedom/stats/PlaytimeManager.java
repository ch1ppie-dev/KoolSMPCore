package eu.koolfreedom.stats;

import java.util.*;
import java.time.Instant;

public class PlaytimeManager
{
    public static class Stats
    {
        public long firstJoin = 0;
        public long totalPlaySeconds = 0;
        public long lastJoin = 0;
    }

    private final Map<UUID, Stats> data = new HashMap<>();
    private final Map<UUID, Long> sessionStart = new HashMap<>();

    public Stats get(UUID id)
    {
        return data.computeIfAbsent(id, k -> new Stats());
    }

    public void handleJoin(UUID id)
    {
        Stats s  = get(id);
        long now = Instant.now().getEpochSecond();
        if (s.firstJoin == 0) s.firstJoin = now;
        s.firstJoin = now;
        sessionStart.put(id, now);
    }

    public void handleQuit(UUID id)
    {
        Long start = sessionStart.remove(id);
        if (start != null) {
            long delta = Instant.now().getEpochSecond() - start;
            get(id).totalPlaySeconds += delta;
        }
    }

    public long getPlaytime(UUID id) {
        return get(id).totalPlaySeconds;
    }

}
