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
//            System.out.println("Fetched Public IP: " + publicIP);

            return publicIP;
        }
        catch (Exception e)
        {
//            System.out.println("Failed to fetch public IP: " + e.getMessage());
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

//        System.out.println("Checking if server IP " + serverIP + " is blacklisted...");

        boolean result = blacklist.contains(serverIP);

//        System.out.println("Blacklist Check Result: " + result);

        return result;
    }

}

