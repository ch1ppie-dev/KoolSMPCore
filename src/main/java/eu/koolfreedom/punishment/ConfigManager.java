package eu.koolfreedom.punishment;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager
{
    private final JavaPlugin plugin;
    private File punishmentsFile;
    private FileConfiguration punishmentsConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        createPunishmentsConfig();
    }

    private void createPunishmentsConfig() {
        punishmentsFile = new File(plugin.getDataFolder(), "punishments.yml");

        if (!punishmentsFile.exists()) {
            try {
                punishmentsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create punishments.yml!");
                e.printStackTrace();
            }
        }
        punishmentsConfig = YamlConfiguration.loadConfiguration(punishmentsFile);
    }

    public FileConfiguration getPunishmentsConfig() {
        return punishmentsConfig;
    }

    public void savePunishmentsConfig() {
        try {
            punishmentsConfig.save(punishmentsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save punishments.yml!");
            e.printStackTrace();
        }
    }

    public void reloadPunishmentsConfig() {
        punishmentsConfig = YamlConfiguration.loadConfiguration(punishmentsFile);
    }
}

