package eu.koolfreedom.listener;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.punishment.PunishmentManager;
import eu.koolfreedom.punishment.PunishmentType;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.UUID;

public class ChatListener implements Listener {
    private final PunishmentManager punishmentManager;

    public ChatListener(PunishmentManager punishmentManager) {
        this.punishmentManager = punishmentManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (punishmentManager.isPunished(playerUUID, PunishmentType.MUTE) ||
                punishmentManager.isPunished(playerUUID, PunishmentType.TEMP_MUTE)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You are muted and cannot speak.");
        }
    }
}

