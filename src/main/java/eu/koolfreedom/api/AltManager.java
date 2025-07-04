package eu.koolfreedom.api;

import java.util.*;

public class AltManager
{
    private final Map<String, Set<UUID>> ipMap = new HashMap<>();

    public void record(String ip, UUID uuid)
    {
        ipMap.computeIfAbsent(ip, k -> new HashSet<>()).add(uuid);
    }

    public Set<UUID> getAlts(String ip)
    {
        return ipMap.getOrDefault(ip, Set.of());
    }

    public Set<UUID> getAccounts(String ip) {
        return getAlts(ip);           // alias – returns Set<UUID>
    }

    public Optional<String> getLastIP(UUID uuid) {
        return ipMap.entrySet().stream()
                .filter(e -> e.getValue().contains(uuid))
                .map(Map.Entry::getKey)
                .findFirst();
    }
}
