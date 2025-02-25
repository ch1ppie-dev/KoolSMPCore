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

public class BlacklistManager {
    private final JavaPlugin plugin;
    private final Set<String> blacklist = new HashSet<>();

    public BlacklistManager(JavaPlugin plugin) {
        this.plugin = plugin;
        fetchBlacklist();
    }

    private void fetchBlacklist()
    {
        try
        {
            URL url = new URL("https://gist.githubusercontent.com/KoolFreedom/ed620abbb65436673aeaa6fc9c428051/raw/dc884c9312525a626f8f5085048d69167b36b4fb/blacklist.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;

            System.out.println("Fetching Blacklist...");

            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#"))
                {
                    blacklist.add(line);
                    System.out.println("Blacklisted IP: " + line);
                }
            }
            reader.close();
        }
        catch (Exception e)
        {
            System.out.println("Could not fetch blacklist: " + e.getMessage());
        }
    }


    public String getPublicIP()
    {
        try
        {
            URL url = new URL("https://api.ipify.org");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String publicIP = in.readLine();
            in.close();

            // Debugging
            System.out.println("Fetched Public IP: " + publicIP);

            return publicIP;
        }
        catch (Exception e)
        {
            System.out.println("Failed to fetch public IP: " + e.getMessage());
            return null;
        }
    }



    public boolean isBlacklisted()
    {
        String serverIP = getPublicIP();
        if (serverIP == null || serverIP.isEmpty())
        {
            serverIP = "127.0.0.1"; // Default for localhost testing
        }

        System.out.println("Checking if server IP " + serverIP + " is blacklisted...");

        boolean result = blacklist.contains(serverIP);

        System.out.println("Blacklist Check Result: " + result);

        return result;
    }

}

