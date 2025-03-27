package eu.koolfreedom.punishment;

import java.util.UUID;

public class Punishment
{
    private final UUID playerUUID;
    private final String reason;
    private final PunishmentType type;
    private final long expiration;

    public Punishment(UUID playerUUID, PunishmentType type, String reason, long duration) {
        this.playerUUID = playerUUID;
        this.type = type;
        this.reason = reason;
        this.expiration = (duration > 0) ? System.currentTimeMillis() + duration : -1;
    }

    public UUID getPlayerUUID() { return playerUUID; }
    public PunishmentType getType() { return type; }
    public String getReason() { return reason; }
    public long getExpiration() { return expiration; }

    public boolean isExpired() {
        return expiration > 0 && System.currentTimeMillis() > expiration;
    }
}
