package eu.koolfreedom.punishment;

import eu.koolfreedom.KoolSMPCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Punishments extends YamlConfiguration
{
    private static Punishments punishments;

    public static Punishments getConfig()
    {
        if (punishments == null)
        {
            punishments = new Punishments();
        }
        return punishments;
    }

    private KoolSMPCore plugin;
    private File configuration;

    public Punishments()
    {
        plugin = KoolSMPCore.getInstance();
        configuration = new File(plugin.getDataFolder(), "punishments.yml");
        saveDefault();
        reload();
    }

    public void reload()
    {
        try
        {
            super.load(configuration);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void save()
    {
        try
        {
            super.save(configuration);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void saveDefault()
    {
        plugin.saveResource("punishments.yml", false);
    }
}
