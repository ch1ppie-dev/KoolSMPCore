package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.discord.Discord;
import eu.koolfreedom.config.ConfigEntry;

import java.awt.*;
import java.time.ZonedDateTime;
import java.util.List;

import eu.koolfreedom.log.FLog;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportCommand extends KoolCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length <= 1)
        {
            return false;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.isOnline() && !target.hasPlayedBefore())
        {
            msg(sender, playerNotFound);
            return true;
        }

        String reason = String.join(" ", ArrayUtils.remove(args, 0));

        broadcast("kf.admin", ConfigEntry.FORMATS_REPORT.getString(),
                Placeholder.parsed("reporter", sender.getName()),
                Placeholder.parsed("player", target.getName() != null ? target.getName() : target.getUniqueId().toString()),
                Placeholder.unparsed("reason", reason));

        if (Discord.getJDA() != null && !ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString().isBlank())
        {
            TextChannel channel = Discord.getJDA().getTextChannelById(ConfigEntry.DISCORD_REPORT_CHANNEL_ID.getString());

            if (channel != null)
            {
                try
                {
                    channel.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("Report for " + target.getName() + (!target.isOnline() ? " (offline)" : ""))
                            .setColor(Color.RED)
                            .setDescription(reason)
                            .setFooter("Reported by " + sender.getName(), "https://minotar.net/helm/" + sender.getName() + ".png")
                            .setTimestamp(ZonedDateTime.now())
                            .build()).queue();
                }
                catch (Exception ex)
                {
                    FLog.error("Failed to send report data to Discord", ex);
                }
            }
        }

        msg(sender, "<green>Thank you. Your report has been logged.");
        msg(sender, "<yellow>Please keep in mind that spamming reports is not allowed, and you will be sanctioned if you do so.");

        if (target.equals(playerSender))
        {
            msg(sender, "<red>But why in the world would you report yourself???");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String commandLabel, String[] args)
    {
        return args.length == 1 ? Bukkit.getOnlinePlayers().stream().map(Player::getName).toList() : List.of();
    }
}