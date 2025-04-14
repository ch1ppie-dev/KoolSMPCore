package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.discord.Discord;
import eu.koolfreedom.config.ConfigEntry;
import java.time.Instant;
import java.time.ZonedDateTime;

import eu.koolfreedom.log.FLog;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReportCommand implements CommandExecutor
{
    public boolean onCommand(@NotNull CommandSender playerReporter, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(playerReporter instanceof Player player)) {
            playerReporter.sendMessage(Messages.ONLY_IN_GAME);
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(KoolSMPCore.main.mmDeserialize("<red>Usage: /" + s + " <player> [reason]"));
            return true;
        }

        Player reportedPlayer = Bukkit.getPlayer(args[0]);
        if (reportedPlayer == null) {
            player.sendMessage(Messages.PLAYER_NOT_FOUND);
            return true;
        }

        String report = StringUtils.join(ArrayUtils.subarray(args, 1, args.length), " ");
        if (report.isBlank()) {
            player.sendMessage(Messages.INVALID_REASON);
            return true;
        }

        playerReporter.sendMessage(Component.newline()
                .append(KoolSMPCore.main.mmDeserialize("<green>Successfully submitted your report to staff members"))
                .appendNewline()
                .append(KoolSMPCore.main.mmDeserialize("<gold>Be mindful that spam reporting is not allowed, and you will be punished if caught doing so.")));

        if (player.getName().equals(reportedPlayer.getName()))
            player.sendMessage(Component.newline()
                    .append(KoolSMPCore.main.mmDeserialize("<red>But why would you report yourself???")));

        TextChannel channel = Discord.getJDA().getTextChannelById(ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString());
        if (channel != null) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Report for " + reportedPlayer.getName() + (reportedPlayer.isOnline() ? "" : " (offline)"));
            embed.setColor(0xFF0000);
            embed.setDescription("Reason: " + report);
            embed.setFooter("Reported by " + playerReporter.getName(), "https://minotar.net/helm/" + playerReporter.getName() + ".png");
            embed.setTimestamp(Instant.from(ZonedDateTime.now()));

            channel.sendMessageEmbeds(embed.build()).queue();
        }

        for (Player players : Bukkit.getOnlinePlayers())
        {
            if (!players.hasPermission("kf.admin"))
                continue;
            players.sendMessage(Component.newline()
                    .append(KoolSMPCore.main.mmDeserialize("<dark_gray>[" + "<red>REPORTS" + "<dark_gray>]<gold> " + playerReporter.getName() + " <red>has repoted <gold>" + reportedPlayer.getName() + " <red>for <gold>" + report))
                    .appendNewline());
        }
        FLog.info("[REPORTS] " + playerReporter.getName() + " has reported " + reportedPlayer.getName() + " for " + report);
        return true;
    }
}