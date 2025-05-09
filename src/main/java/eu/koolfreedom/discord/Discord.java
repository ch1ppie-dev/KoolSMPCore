package eu.koolfreedom.discord;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.event.PlayerReportDeleteEvent;
import eu.koolfreedom.event.PlayerReportEvent;
import eu.koolfreedom.event.PlayerReportUpdateEvent;
import eu.koolfreedom.log.FLog;
import eu.koolfreedom.reporting.Report;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Discord extends ListenerAdapter implements Listener
{
    private JDA jda;

    public Discord()
    {
        final String token = ConfigEntry.DISCORD_BOT_TOKEN.getString();

        // No token could be found, so we're going to assume the user doesn't want it
        if (token == null || token.isEmpty())
        {
            return;
        }

        try
        {
            jda = JDABuilder.createLight(token)
                    .setEnabledIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .setDisabledIntents(GatewayIntent.GUILD_VOICE_STATES)
                    .build()
                    .awaitReady();

            FLog.info("Discord bot initialized");
        }
        catch (Exception ex)
        {
            FLog.error("Failed to initialize Discord bot", ex);
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, KoolSMPCore.getInstance());
        jda.addEventListener(this);
    }

    public void shutdown()
    {
        if (jda != null)
        {
            jda.shutdownNow();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event)
    {
        FLog.warning("Hi there, would you like to sign my petition?");

        if (event.getChannelId() == null
                || !event.getChannelId().equalsIgnoreCase(ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString())
                || event.getGuild() == null || !event.getGuild().getId().equalsIgnoreCase(ConfigEntry.DISCORD_SERVER_ID.getString()))
        {
            FLog.warning("channel is null, guild is null, channel is not report channel, or server is not the guild we were expecting");
            return;
        }

        final Message message = event.getMessage();
        final Button button = event.getButton();
        final User user = event.getUser();
        final Component userDisplay = Component.text(user.getAsMention());
        final Optional<Report> optionalReport = KoolSMPCore.getInstance().reportManager.getReports(true).stream().filter(report ->
                report.getAdditionalData().getString("discordMessageId", "-1").equalsIgnoreCase(message.getId())).findAny();

        if (message.getAuthor() == jda.getSelfUser() && button.getId() != null)
        {
            switch (button.getId().toLowerCase())
            {
                case "handled" -> optionalReport.ifPresentOrElse(report ->
                {
                    FLog.warning("Resolve picked");
                    if (report.isResolved())
                    {
                        event.reply("This report has already been resolved.").setEphemeral(true).queue();
                        return;
                    }

                    report.updateAsync(userDisplay, user.getName(), user.getId(), Report.ReportStatus.RESOLVED, "Handled");
                    event.reply("Report " + report.getId() + " has been re-handled.").setEphemeral(true).queue();
                }, () -> message.editMessage(MessageEditData.fromCreateData(new MessageCreateBuilder()
                        .addContent("Unlinked report resolved by " + user.getAsMention())
                        .addEmbeds(message.getEmbeds())
                        .build())).queue());
                case "invalid" -> optionalReport.ifPresentOrElse(report ->
                {
                    FLog.warning("Invalid picked");
                    if (report.isResolved())
                    {
                        FLog.warning("No way you freaking pinko");
                        event.reply("This report has already been resolved.").setEphemeral(true).queue();
                        return;
                    }

                    FLog.warning("Okay, I guess that sounds good");
                    report.updateAsync(userDisplay, user.getName(), user.getId(), Report.ReportStatus.CLOSED, "Invalid");
                    event.reply("Report " + report.getId() + " has been closed.").setEphemeral(true).queue();
                }, () ->
                {
                    message.delete().queue();
                    event.reply("This report didn't even have a message ID associated to it somehow, so we deleted it.").setEphemeral(true).queue();
                });
                case "purge" -> optionalReport.ifPresentOrElse(report ->
                {
                    FLog.warning("Purge picked");
                    KoolSMPCore.getInstance().reportManager.deleteReportsByUuidAsync(userDisplay, user.getName(), user.getId(), report.getReporter());
                    event.reply("Reports are now being deleted as we speak.").setEphemeral(true).queue();
                }, () ->
                {
                    message.delete().queue();
                    event.reply("This report didn't even have a message ID associated to it somehow, so we couldn't delete every report this user filed.").setEphemeral(true).queue();
                });
                case "reopen" -> optionalReport.ifPresentOrElse(report ->
                {
                    FLog.warning("Reopen picked");
                    if (!report.isResolved())
                    {
                        event.reply("This report is already open.").setEphemeral(true).queue();
                        return;
                    }

                    report.updateAsync(userDisplay, user.getName(), user.getId(), Report.ReportStatus.REOPENED, "Reopened for further investigation");
                    event.reply("Report " + report.getId() + " has been re-opened.").setEphemeral(true).queue();
                }, () -> event.reply("This report doesn't have a message ID associated to it, so we can't re-open it.").setEphemeral(true).queue());
            }
        }
        else
        {
            FLog.warning("What the fuck?");
        }
    }

    @EventHandler
    private void onReport(PlayerReportEvent event)
    {
        final Report report = event.getReport();

        Guild guild = jda.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());

        if (guild != null)
        {
            TextChannel channel = guild.getTextChannelById(ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString());

            if (channel != null)
            {
                try
                {
                    channel.sendMessageEmbeds(createEmbedFromReport(report))
                            .addActionRow(createButtonsFromReport(report))
                            .queue(message ->
                            {
                                report.getAdditionalData().set("discordMessageId", message.getId());
                                CompletableFuture.runAsync(() -> KoolSMPCore.getInstance().reportManager.save());
                            });
                }
                catch (Exception ex)
                {
                    FLog.error("Failed to send report data to Discord", ex);
                }
            }
        }
    }

    @EventHandler
    private void onReportUpdated(PlayerReportUpdateEvent event)
    {
        final Report report = event.getReport();

        if (!report.getAdditionalData().isString("discordMessageId"))
        {
            return;
        }

        Guild guild = jda.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());

        if (guild != null)
        {
            TextChannel channel = guild.getTextChannelById(ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString());

            if (channel != null)
            {
                channel.retrieveMessageById(Objects.requireNonNull(report.getAdditionalData().getString("discordMessageId"))).queue(message ->
						message.editMessage(MessageEditData.fromCreateData(new MessageCreateBuilder()
								.addContent("Report updated by " + event.getStaffName())
								.addEmbeds(createEmbedFromReport(report))
								.addActionRow(createButtonsFromReport(report))
								.build())).queue(),
                        (ignored) ->
								channel.sendMessage("Report updated by " + event.getStaffName())
										.addEmbeds(createEmbedFromReport(report))
										.addActionRow(createButtonsFromReport(report))
										.queue(success ->
                                                {
                                                    report.getAdditionalData().set("discordMessageId", success.getId());
                                                    CompletableFuture.runAsync(() -> KoolSMPCore.getInstance().reportManager.save());
                                                }));
            }
        }
    }

    @EventHandler
    private void onReportDeleted(PlayerReportDeleteEvent event)
    {
        Guild guild = jda.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());

        if (guild != null)
        {
            TextChannel channel = guild.getTextChannelById(ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString());

            if (channel != null)
            {
                channel.purgeMessagesById(event.getReports().stream().filter(report -> report.getAdditionalData().isString("discordMessageId"))
                        .map(report -> report.getAdditionalData().getString("discordMessageId")).toList());
            }
        }
    }

    private List<Button> createButtonsFromReport(Report report)
    {
        return report.isResolved() ?
                List.of(Button.primary("reopen", "Re-open")) :
                //--
                List.of(Button.primary("handled", "Resolve"),
                        Button.primary("invalid", "Invalid"),
                        Button.danger("purge", "Purge"));
    }

    private MessageEmbed createEmbedFromReport(Report report)
    {
        final OfflinePlayer reporter = Bukkit.getOfflinePlayer(report.getReporter());
        final OfflinePlayer reported = Bukkit.getOfflinePlayer(report.getReported());
        final String reason = report.getReason();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle((report.getStatus() != Report.ReportStatus.UNRESOLVED ? "**" + report.getStatus().name() + "**: " : "") + "Report for " + reported.getName() + (!reported.isOnline() ? " (offline)" : "") )
                .setColor(report.getStatus().getAwtColor())
                .setDescription(reason)
                .setFooter("ID " + report.getId() + " • Reported by " + reporter.getName(), "https://minotar.net/helm/" + reporter.getName() + ".png")
                .setTimestamp(ZonedDateTime.now());

        if (report.getLastNote() != null)
        {
            embed.addField("Staff Note", report.getLastNote(), true);
        }

        return embed.build();
    }
}
