package eu.koolfreedom.discord;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.log.FLog;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.time.Instant;

public class DiscordLogger {

    public static void sendStaffAction(StaffActionType type, String actor, String target, String reason) {
        TextChannel channel = KoolSMPCore.jda.getTextChannelById(ConfigEntry.DISCORD_STAFF_ACTION_CHANNEL_ID.getString());
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Staff Action - " + type.getLabel())
                .setColor(type.getColor())
                .addField("Target", target, true)
                .addField("By", actor, true)
                .addField("Reason", reason == null || reason.isBlank() ? "None specified" : reason, false)
                .setTimestamp(Instant.now());

        channel.sendMessageEmbeds(embed.build()).queue();

        if (KoolSMPCore.jda == null) {
            FLog.warning("[DiscordLogger] Tried to log a staff action, but Discord bot is not initialized.");
            return;
        }


        if (channel != null) {
            channel.sendMessageEmbeds(embed.build()).queue();
        } else {
            FLog.warning("[DiscordLogger] Staff channel ID is invalid or missing in config.");
        }
    }

}
