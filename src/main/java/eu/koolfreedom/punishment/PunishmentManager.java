package eu.koolfreedom.punishment;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class PunishmentManager
{
    private final Map<UUID, Punishment> punishments = new HashMap<>();
    private final ConfigManager configManager;
    private final FileConfiguration config;

    public PunishmentManager(JavaPlugin plugin) {
        this.configManager = new ConfigManager(plugin);
        this.config = configManager.getPunishmentsConfig();
    }

    public void addPunishment(UUID playerUUID, String type, String reason, long expiration) {
        config.set("punishments." + playerUUID + ".type", type);
        config.set("punishments." + playerUUID + ".reason", reason);
        config.set("punishments." + playerUUID + ".expiration", expiration);
        configManager.savePunishmentsConfig();
    }

    public void removePunishment(UUID playerUUID) {
        config.set("punishments." + playerUUID, null);
        configManager.savePunishmentsConfig();
    }

    public boolean isPunished(UUID playerUUID, PunishmentType type) {
       return config.contains("punishments." + playerUUID);
    }

    public void enforcePunishment(Punishment punishment) {
        Player player = Bukkit.getPlayer(punishment.getPlayerUUID());
        if (player == null) return;

        switch (punishment.getType()) {
            case BAN:
            case TEMP_BAN:
                player.kickPlayer(ChatColor.RED + "You are banned!\nReason: " + punishment.getReason());
                break;
            case MUTE:
            case TEMP_MUTE:
                player.sendMessage(ChatColor.RED + "You are muted! Reason: " + punishment.getReason());
                break;
            case WARN:
                player.sendMessage(ChatColor.YELLOW + "Warning: " + punishment.getReason());
                break;
            case KICK:
                player.kickPlayer(ChatColor.RED + "You were kicked! Reason: " + punishment.getReason());
                break;
        }
    }

    public void checkExpirations() {
        punishments.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public Set<UUID> getAllPunishedPlayers() {
        Set<UUID> punishedPlayers = new HashSet<>();
        if (config.contains("punishments")) {
            for (String uuidStr : config.getConfigurationSection("punishments").getKeys(false)) {
                punishedPlayers.add(UUID.fromString(uuidStr));
            }
        }
        return punishedPlayers;
    }
}
