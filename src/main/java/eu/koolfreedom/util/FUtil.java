package eu.koolfreedom.util;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.log.FLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.tag.Inserting;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.PreProcess;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.examination.Examinable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class FUtil // the f stands for fuck
{
    private static final Random RANDOM = new Random();

    @Deprecated(forRemoval = true)
    public static void adminAction(String adminName, String action, boolean isRed)
    {
        FUtil.bcastMsg(adminName + " - " + action, (isRed ? ChatColor.RED : ChatColor.AQUA));
    }

    @Deprecated(forRemoval = true)
    public static void bcastMsg(String message, ChatColor color)
    {
        FLog.info(message);

        for (Player player : Bukkit.getOnlinePlayers())
        {
            player.sendMessage((color == null ? "" : color) + message);
        }
    }

    public static void staffAction(CommandSender sender, String message, TagResolver... placeholders)
    {
        broadcast("<gray><i>[<sender>: <action>]", Placeholder.unparsed("sender", sender.getName()),
                Placeholder.component("action", KoolSMPCore.main.mmDeserialize(message, placeholders)));
    }

    public static void broadcast(Component component)
    {
        Bukkit.broadcast(component);
    }

    public static void broadcast(String message, TagResolver... placeholders)
    {
        Bukkit.broadcast(KoolSMPCore.main.mmDeserialize(message, placeholders));
    }

    public static int randomNumber()
    {
        return RANDOM.nextInt();
    }

    public static int randomNumber(int min, int max)
    {
        return RANDOM.nextInt(min, max);
    }

    public static void adminChat(CommandSender sender, String message)
    {
        Component formattedMessage = KoolSMPCore.main.mmDeserialize(ConfigEntry.FORMATS_ADMIN_CHAT.getString(),
                Placeholder.unparsed("name", sender.getName()),
                Placeholder.component("rank", KoolSMPCore.main.perms.getSenderGroup(sender).getColoredName()),
                Placeholder.unparsed("message", message));

        FLog.info(formattedMessage);

        Bukkit.getOnlinePlayers()
                .stream()
                .filter((players) -> (players.hasPermission("kf.admin")))
                .forEachOrdered((players) -> players.sendMessage(formattedMessage));
    }

    public static String getIp(Player player)
    {
        return player.getAddress().getAddress().getHostAddress().trim();
    }

    public static class RandomColorTag implements Modifying
    {
        public static final RandomColorTag INSTANCE = new RandomColorTag();

        @Override
        public Component apply(@NotNull Component current, int depth)
        {
            if (current instanceof TextComponent textComponent)
            {
                return Component.join(JoinConfiguration.spaces(), Arrays.stream(textComponent.content().split(" "))
                        .map(text -> Component.text(text).color(TextColor.color(randomNumber(0, 255),
                                randomNumber(0, 255), randomNumber(0, 255)))).toList());
            }

            return current;
        }
    }
}
