package eu.koolfreedom.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    private final Plugin plugin;
    private final String repoOwner;
    private final String repoName;

    public UpdateChecker(Plugin plugin, String repoOwner, String repoName) {
        this.plugin = plugin;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
    }

    public void check() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.github.com/repos/" + repoOwner + "/" + repoName + "/releases/latest");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept", "application/vnd.github+json");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    String json = response.toString();
                    String latestTag = json.split("\"tag_name\":\"")[1].split("\"")[0];

                    String currentVersion = plugin.getDescription().getVersion();
                    if (!latestTag.equalsIgnoreCase("v" + currentVersion)) {
                        plugin.getLogger().warning("------------------------------------------------");
                        plugin.getLogger().warning("An update is available for " + plugin.getName() + "!");
                        plugin.getLogger().warning("Current version: v" + currentVersion);
                        plugin.getLogger().warning("Latest version: " + latestTag);
                        plugin.getLogger().warning("Download: https://github.com/" + repoOwner + "/" + repoName + "/releases/latest");
                        plugin.getLogger().warning("------------------------------------------------");
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }
}

