package eu.koolfreedom.discord;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.api.GroupCosmetics;
import eu.koolfreedom.event.PlayerReportDeleteEvent;
import eu.koolfreedom.event.PlayerReportEvent;
import eu.koolfreedom.event.PlayerReportUpdateEvent;
import eu.koolfreedom.event.PublicBroadcastEvent;
import eu.koolfreedom.reporting.Report;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.ButtonClickEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button;
import github.scarsz.discordsrv.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class Discord extends ListenerAdapter implements Listener
{
    private final GroupCosmetics.Group group = GroupCosmetics.Group.createGroup("discord", "Discord",
            Component.text("Discord").color(TextColor.color(0x5865F2)), TextColor.color(0x5865F2));
    private final DiscordSRV plugin;

    private final GsonComponentSerializer nativeAdventureSerializer = GsonComponentSerializer.gson();
    private final github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.gson.GsonComponentSerializer srvAdventureSerializer = github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson();

    public Discord()
    {
        plugin = DiscordSRV.getPlugin();
        Bukkit.getPluginManager().registerEvents(this, KoolSMPCore.getInstance());
        DiscordSRV.api.subscribe(this);
    }

    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event)
    {
        plugin.getJda().addEventListener(this);
    }

    @Override
    public void onButtonClick(ButtonClickEvent event)
    {
        if (channelDoesNotMatch("reports", event.getChannel().getId())
                || event.getMember() == null
                || event.getButton() == null)
        {
            return;
        }

        final Message message = event.getMessage();
        final Button button = event.getButton();
        final Member member = event.getMember();
        final Component userDisplay = Component.text(member.getAsMention());
        final Optional<Report> optionalReport = KoolSMPCore.getInstance().reportManager.getReports(true).stream().filter(report ->
                report.getAdditionalData().getString("discordMessageId", "-1").equalsIgnoreCase(message.getId())).findAny();

        if (message.getAuthor() == plugin.getJda().getSelfUser() && button.getId() != null)
        {
            switch (button.getId().toLowerCase())
            {
                case "handled" ->
                {
                    if (!hasPermission(member, "kfc.command.reports.handle"))
                    {
                        event.reply("You don't have permission to do that.").setEphemeral(true).queue();
                        return;
                    }

                    optionalReport.ifPresentOrElse(report ->
                    {
                        if (report.isResolved())
                        {
                            event.reply("This report has already been resolved.").setEphemeral(true).queue();
                            return;
                        }

                        report.updateAsync(userDisplay, member.getUser().getName(), member.getUser().getId(), Report.ReportStatus.RESOLVED, "Handled");
                        event.reply("Report " + report.getId() + " has been marked as handled.").setEphemeral(true).queue();
                    }, () -> message.editMessage("Unlinked report resolved by " + member.getAsMention())
                            .setEmbeds(message.getEmbeds()).queue());
                }
                case "invalid" ->
                {
                    if (!hasPermission(member, "kfc.command.reports.close"))
                    {
                        event.reply("You don't have permission to do that.").setEphemeral(true).queue();
                        return;
                    }

                    optionalReport.ifPresentOrElse(report ->
                    {
                        if (report.isResolved())
                        {
                            event.reply("This report has already been resolved.").setEphemeral(true).queue();
                            return;
                        }

                        report.updateAsync(userDisplay, member.getUser().getName(), member.getUser().getId(), Report.ReportStatus.CLOSED, "Invalid");
                        event.reply("Report " + report.getId() + " has been closed.").setEphemeral(true).queue();
                    }, () ->
                    {
                        message.delete().queue();
                        event.reply("This report didn't even have a message ID associated to it somehow, so we deleted it.").setEphemeral(true).queue();
                    });
                }
                case "purge" ->
                {
                    if (!hasPermission(member, "kfc.command.reports.purge"))
                    {
                        event.reply("You don't have permission to do that.").setEphemeral(true).queue();
                        return;
                    }

                    optionalReport.ifPresentOrElse(report ->
                    {
                        KoolSMPCore.getInstance().reportManager.deleteReportsByUuidAsync(userDisplay, member.getUser().getName(), member.getUser().getId(), report.getReporter());
                        event.reply("Reports are now being deleted as we speak.").setEphemeral(true).queue();
                    }, () ->
                    {
                        message.delete().queue();
                        event.reply("This report didn't even have a message ID associated to it somehow, so we couldn't delete every report this user filed.").setEphemeral(true).queue();
                    });
                }
                case "reopen" ->
                {
                    if (!hasPermission(member, "kfc.command.reports.reopen"))
                    {
                        event.reply("You don't have permission to do that.").setEphemeral(true).queue();
                        return;
                    }

                    optionalReport.ifPresentOrElse(report ->
                    {
                        if (!report.isResolved())
                        {
                            event.reply("This report is already open.").setEphemeral(true).queue();
                            return;
                        }

                        report.updateAsync(userDisplay, member.getUser().getName(), member.getUser().getId(), Report.ReportStatus.REOPENED, "Reopened for further investigation");
                        event.reply("Report " + report.getId() + " has been re-opened.").setEphemeral(true).queue();
                    }, () -> event.reply("This report doesn't have a message ID associated to it, so we can't re-open it.").setEphemeral(true).queue());
                }
            }
        }
    }

    @EventHandler
    private void onReport(PlayerReportEvent event)
    {
        if (!channelExists("reports"))
        {
            return;
        }

        final Report report = event.getReport();

        plugin.getOptionalTextChannel("reports")
                .sendMessageEmbeds(createEmbedFromReport(report))
                .setActionRow(createButtonsFromReport(report))
                .queue(message ->
                {
                    report.getAdditionalData().set("discordMessageId", message.getId());
                    CompletableFuture.runAsync(() -> KoolSMPCore.getInstance().reportManager.save());
                });
    }

    @EventHandler
    private void onReportUpdated(PlayerReportUpdateEvent event)
    {
        if (!channelExists("reports"))
        {
            return;
        }

        final Report report = event.getReport();
        final TextChannel channel = plugin.getOptionalTextChannel("reports");
        AtomicBoolean createMessage = new AtomicBoolean(false);

        if (!report.getAdditionalData().isString("discordMessageId"))
        {
            createMessage.set(true);
        }

        channel.retrieveMessageById(Objects.requireNonNull(report.getAdditionalData().getString("discordMessageId")))
                .queue(message ->
                        message.editMessage("Report updated by " + (event.getStaffDisplayName() instanceof TextComponent text ? text.content() : event.getStaffName()))
                                .setEmbeds(createEmbedFromReport(report))
                                .setActionRow(createButtonsFromReport(report)).queue(),
                        error -> createMessage.set(true));

        if (createMessage.get())
        {
            channel.sendMessage("Report updated by " + event.getStaffName())
                    .setEmbeds(createEmbedFromReport(report))
                    .setActionRow(createButtonsFromReport(report))
                    .queue(success ->
                    {
                        report.getAdditionalData().set("discordMessageId", success.getId());
                        CompletableFuture.runAsync(() -> KoolSMPCore.getInstance().reportManager.save());
                    });
        }
    }

    @EventHandler
    private void onReportDeleted(PlayerReportDeleteEvent event)
    {
        if (!channelExists("reports"))
        {
            return;
        }

        final TextChannel channel = plugin.getOptionalTextChannel("reports");
        channel.purgeMessagesById(event.getReports().stream().filter(report -> report.getAdditionalData().isString("discordMessageId"))
                .map(report -> report.getAdditionalData().getString("discordMessageId")).toList());
    }

    @EventHandler
    private void onBroadcast(PublicBroadcastEvent event)
    {
        final TextChannel channel = plugin.getOptionalTextChannel("broadcasts");
        Component message = event.getMessage();

        if (message.decorations().entrySet().stream().allMatch(entry -> entry.getValue().equals(TextDecoration.State.NOT_SET)
                || entry.getValue().equals(TextDecoration.State.FALSE)))
        {
            message = message.decorate(net.kyori.adventure.text.format.TextDecoration.BOLD);
        }

        channel.sendMessage(MessageUtil.reserializeToDiscord(convert(message))).queue();
    }

    private boolean channelDoesNotMatch(String name, String channelId)
    {
        return !channelId.equals(plugin.getChannels().get(name));
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

    private github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component convert(Component component)
    {
        return srvAdventureSerializer.deserialize(nativeAdventureSerializer.serialize(component));
    }

    public boolean channelExists(String name)
    {
        return plugin.getChannels().containsKey(name);
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

    private boolean hasPermission(Member member, String permission)
    {
        final UUID uuid = plugin.getAccountLinkManager().getUuid(member.getId());

        if (uuid == null)
        {
            return false;
        }

        final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (!player.isOnline() && !player.hasPlayedBefore())
        {
            return false;
        }

        return KoolSMPCore.getInstance().groupCosmetics.getVaultPermissions().playerHas(null, player, permission);
    }
}
