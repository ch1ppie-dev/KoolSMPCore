package eu.koolfreedom.discord;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.api.GroupCosmetics;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.event.*;
import eu.koolfreedom.reporting.Report;
import eu.koolfreedom.util.FUtil;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.*;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.ButtonClickEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button;
import github.scarsz.discordsrv.util.*;
import net.kyori.adventure.key.Namespaced;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
public class Discord extends ListenerAdapter implements Listener
{
    private final GroupCosmetics.Group group = GroupCosmetics.Group.createGroup("discord", "Discord",
            Component.text("Discord").color(TextColor.color(0x5865F2)), TextColor.color(0x5865F2));

    private final Map<String, GroupCosmetics.Group> roleMap = new HashMap<>();

    private final DiscordSRV plugin;
    private final Namespaced key;

    private final GsonComponentSerializer nativeAdventureSerializer = GsonComponentSerializer.gson();
    private final github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.gson.GsonComponentSerializer srvAdventureSerializer = github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson();

    public Discord()
    {
        plugin = DiscordSRV.getPlugin();
        key = NamespacedKey.fromString("channel", plugin);

        Bukkit.getPluginManager().registerEvents(this, KoolSMPCore.getInstance());
        DiscordSRV.api.subscribe(this);
    }

    // -- DISCORDSRV EVENTS -- //

    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event)
    {
        plugin.getJda().addEventListener(this);
    }

    @Subscribe
    public void onAdminChatMessageFromDiscord(DiscordGuildMessagePreProcessEvent event)
    {
        if (!channelExists("adminchat") || channelDoesNotMatch("adminchat", event.getChannel().getId())
                || event.getAuthor() == plugin.getJda().getSelfUser())
        {
            return;
        }

        final GroupCosmetics.Group fallback = KoolSMPCore.getInstance().groupCosmetics.getGroupByNameOr("discord", group);

        final Member member = event.getMember();
        final Component message = convert(MessageUtil.reserializeToMinecraft(event.getMessage().getContentRaw()));
        final GroupCosmetics.Group userGroup = switch (ConfigEntry.DISCORDSRV_GROUP_MODE_SWITCH.getInteger())
        {
            case 1 -> KoolSMPCore.getInstance().groupCosmetics.getGroupByNameOr(member.getRoles().getFirst().getName(),
                    !member.getRoles().isEmpty() ? getGroupFromRole(member.getRoles().getFirst()) : fallback);
            case 2 -> !member.getRoles().isEmpty() ? getGroupFromRole(member.getRoles().getFirst()) : fallback;
            default -> fallback;
        };

        Component displayName = FUtil.miniMessage(ConfigEntry.DISCORDSRV_USER_FORMAT.getString(),
                Placeholder.parsed("id", member.getId()),
                Placeholder.parsed("username", member.getUser().getName()),
                Placeholder.parsed("name", member.getUser().getEffectiveName()),
                Placeholder.unparsed("nickname", member.getEffectiveName()),
                Placeholder.styling("role_color", (builder) -> builder.color(TextColor.color(member.getColorRaw()))),
                Placeholder.component("roles", member.getRoles().isEmpty() ?
                        Component.text("(none)").color(NamedTextColor.GRAY) :
                        Component.text(" - ").color(NamedTextColor.GRAY).append(Component.join(JoinConfiguration.separator(Component.newline()
                                        .append(Component.text(" - ").color(NamedTextColor.GRAY))),
                member.getRoles().stream().map(role -> Component.text(role.getName()).color(TextColor.color(role.getColorRaw()))).toList()))));

        FUtil.asyncAdminChat(displayName, member.getUser().getName(), group, message, key);
    }

    // -- JDA EVENTS -- //

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

    // -- BUKKIT EVENTS -- //

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
                        message.editMessage("Report updated by " + (event.getStaffDisplayName() instanceof TextComponent text && !text.content().isBlank() ? text.content() : event.getStaffName()))
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

    @EventHandler
    public void onAdminChat(AdminChatEvent event)
    {
        // Do not call duplicate events and don't do anything if we do not have a designated adminchat channel to avoid
        //  potential confidentiality leaks
        if (event.getSource().equals(key) || !channelExists("adminchat"))
        {
            return;
        }

        final TextChannel channel = plugin.getOptionalTextChannel("adminchat");
        final String senderName = event.getSenderName();
        final CommandSender sender = event.getCommandSender();

        boolean usingWebhooks = DiscordSRV.config().getBoolean("Experiment_WebhookChatMessageDelivery");

        // Did it come from the server itself
        if (sender instanceof Player player)
        {
            plugin.processChatMessage(player, convert(event.getMessage()), "adminchat", false, event);
            return;
        }

        String displayName;
        String webhookAvatar = plugin.getJda().getSelfUser().getAvatarUrl();
        final String message = MessageUtil.reserializeToDiscord(convert(event.getMessage()));

        // The command came from someone running the command from within console
        if (sender instanceof PaperForwardingCommandSender srv)
        {
            displayName = plugin.getJda().getSelfUser().getEffectiveName();
        }
        // The command came from a
        else if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender)
        {
            displayName = sender.getName();
        }
        else if (sender instanceof Mob mob)
        {
            displayName = mob.customName() instanceof TextComponent text ? text.content() : mob.getName();
        }
        else
        {
            displayName = event.getSenderDisplay() instanceof TextComponent text ? text.content() : senderName;
        }

        // TODO: Implement more... "native" support instead of this hackjob, lmao

        if (DiscordSRV.config().getBoolean("Experiment_WebhookChatMessageDelivery"))
        {
            WebhookUtil.deliverMessage(channel, displayName, webhookAvatar, message, List.of());
        }
        else
        {
            DiscordUtil.sendMessage(channel, LangUtil.Message.CHAT_TO_DISCORD_NO_PRIMARY_GROUP.toString()
                    .replace("%username%", DiscordUtil.escapeMarkdown(senderName))
                    .replace("%usernamenoescapes%", senderName)
                    .replace("%displayname%", DiscordUtil.escapeMarkdown(displayName))
                    .replace("%displaynamenoescapes%", displayName)
                    .replace("%message%", displayName)
            );
        }
    }

    // -- UTILITY/CONVERSION METHODS -- //

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

    private GroupCosmetics.Group getGroupFromRole(Role role)
    {
        final String roleId = role.getId();

        if (!roleMap.containsKey(roleId) || roleMap.get(roleId).getColor().value() != role.getColorRaw() ||
                !roleMap.get(roleId).getName().equalsIgnoreCase(role.getName()))
        {
            roleMap.remove(roleId);

            GroupCosmetics.Group roleGroup = GroupCosmetics.Group.createGroup(role.getId(), role.getName(),
                    Component.text(role.getName()).color(TextColor.color(role.getColorRaw())),
                    TextColor.color(role.getColorRaw()));

            roleMap.put(roleId, roleGroup);
        }

        return roleMap.get(roleId);
    }

    private github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component convert(Component component)
    {
        return srvAdventureSerializer.deserialize(nativeAdventureSerializer.serialize(component));
    }

    private Component convert(github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component component)
    {
        return nativeAdventureSerializer.deserialize(srvAdventureSerializer.serialize(component));
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
