package eu.koolfreedom.system;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import eu.koolfreedom.KoolSMPCore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IndefiniteBanSystem {

    private final FileConfiguration bansConfig;
    private final File bansFile;
    private List<String> bannedPlayers;

    public IndefiniteBanSystem(KoolSMPCore plugin) {
        bannedPlayers = new ArrayList<>();
        bansFile = new File(plugin.getDataFolder(), "indefbans.yml");
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
        loadBannedPlayers();
    }

    public void loadBannedPlayers() {
        if (bansConfig.contains("banned-players")) {
            bannedPlayers = bansConfig.getStringList("banned-players");
        }
    }

    public boolean isPlayerBanned(UUID playerUUID) {
        return bannedPlayers.contains(playerUUID.toString());
    }

    public boolean isPlayerBanned(Player player) {
        return isPlayerBanned(player.getUniqueId());
    }

    public void banPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        String playerUUIDString = playerUUID.toString();
        if (!bannedPlayers.contains(playerUUIDString)) {
            bannedPlayers.add(playerUUIDString);
            saveBannedPlayers();
        }
    }

    public void unbanPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        String playerUUIDString = playerUUID.toString();
        if (bannedPlayers.contains(playerUUIDString)) {
            bannedPlayers.remove(playerUUIDString);
            saveBannedPlayers();
        }
    }

    private void saveBannedPlayers() {
        bansConfig.set("banned-players", bannedPlayers);
        try {
            bansConfig.save(bansFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getBannedPlayers() {
        return bannedPlayers;
    }
}