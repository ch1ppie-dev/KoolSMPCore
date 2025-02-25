package eu.koolfreedom;

import eu.koolfreedom.log.FLog;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class BlacklistManager
{
    private final JavaPlugin plugin;
    private final Set<String> blacklist = new HashSet<>();
    private static final String BLACKLIST_URL = "https://gist.githubusercontent.com/KoolFreedom/ed620abbb65436673aeaa6fc9c428051/raw/01d7af4321f9d31ee4f0b01bde483eafb7757315/blacklist.txt"; // Change this to your private link

    public BlacklistManager(JavaPlugin plugin)
    {
        this.plugin = plugin;
        fetchBlacklist();
    }

    private void fetchBlacklist()
    {
        try
        {
            URL url = new URL(BLACKLIST_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#"))
                {
                    blacklist.add(line);
                }
            }
            reader.close();
        }
        catch (Exception e)
        {
            FLog.warning("Could not fetch online blacklist: " + e.getMessage());
        }
    }

    public boolean isBlacklisted()
    {
        String serverIP = Bukkit.getIp();
        plugin.getLogger().info("Blacklist Check: Server IP detected as: " + serverIP);

        boolean result = blacklist.contains(serverIP);
        plugin.getLogger().info("Blacklist Check: Is blacklisted? " + result);
        return result;
    }
}
