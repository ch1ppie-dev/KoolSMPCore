package eu.koolfreedom.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import java.util.Optional;

public class ChatListener implements Listener {
    private final Plugin plugin;
    private final LuckPerms luckPerms;
    private final LegacyComponentSerializer legacySerializer;

    public ChatListener(Plugin plugin) {
        this.plugin = plugin;
        this.luckPerms = LuckPermsProvider.get();
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand(); // Converts & codes
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        FileConfiguration config = plugin.getConfig();

        if (!config.getBoolean("chat-format.enabled")) {
            return;
        }

        // Check if player has a user-specific format
        String playerName = player.getName().toLowerCase();
        if (config.contains("chat-format.users." + playerName + ".format")) {
            String format = config.getString("chat-format.users." + playerName + ".format", "{message}");
            format = format.replace("{message}", message);

            // Convert & color codes to Adventure API format
            Component formattedMessage = legacySerializer.deserialize(format);
            event.setMessage(formattedMessage.toString());
            return;
        }

        // If no user format, check group format
        String group = getPrimaryGroup(player);
        if (config.contains("chat-format.groups." + group + ".format")) {
            String format = config.getString("chat-format.groups." + group + ".format", "{message}");
            format = format.replace("{message}", message);

            // Convert & color codes to Adventure API format
            Component formattedMessage = legacySerializer.deserialize(format);
            event.setMessage(formattedMessage.toString());
            return;
        }

        // Default format if no specific user or group formatting
        String defaultFormat = config.getString("chat-format.default.format", "{message}");
        defaultFormat = defaultFormat.replace("{message}", message);
        Component defaultMessage = legacySerializer.deserialize(defaultFormat);
        event.setMessage(defaultMessage.toString());
    }

    private String getPrimaryGroup(Player player) {
        Optional<User> user = Optional.ofNullable(luckPerms.getUserManager().getUser(player.getUniqueId()));
        return user.map(User::getPrimaryGroup).orElse("default");
    }
}
