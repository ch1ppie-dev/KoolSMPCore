package eu.koolfreedom.discord;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.config.ConfigEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.time.Instant;

public class DiscordLogger {

    public static void sendStaffAction(StaffActionType type, String actor, String target, String reason) {
        TextChannel channel = KoolSMPCore.jda.getTextChannelById(ConfigEntry.DISCORD_STAFF_ACTION_CHANNEL_ID.getString());
        if (channel == null) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Staff Action - " + type.getLabel())
                .setColor(type.getColor())
                .addField("Target", target, true)
                .addField("By", actor, true)
                .addField("Reason", reason == null || reason.isBlank() ? "None specified" : reason, false)
                .setTimestamp(Instant.now());

        channel.sendMessageEmbeds(embed.build()).queue();
    }

}
