package eu.koolfreedom.api;

import java.awt.*;
import java.util.*;

import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.util.KoolSMPCoreBase;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class GroupCosmetics extends KoolSMPCoreBase
{
    private final Map<String, Group> groups = new HashMap<>();
    private final Group defaultFallbackGroup = new Group("default", "Member", NamedTextColor.GRAY);
    private final Group consoleGroup = new Group("console", "Console", NamedTextColor.BLUE);

    public GroupCosmetics()
    {
        loadGroups();
    }

    public void loadGroups()
    {
        groups.clear();

        ConfigurationSection section = ConfigEntry.GROUPS.getConfigurationSection();
        section.getKeys(false).stream().filter(section::isConfigurationSection).forEach(entry ->
				groups.put(entry, Group.fromConfigurationSection(entry, Objects.requireNonNull(section.getConfigurationSection(entry)))));
    }

    public Group getGroupByName(String name)
    {
        return groups.getOrDefault(name, defaultFallbackGroup);
    }

    public Group getSenderGroup(CommandSender sender)
    {
        if (sender instanceof Player player)
        {
            return getGroupByName(Objects.requireNonNull(api.getUserManager().getUser(player.getUniqueId())).getPrimaryGroup());
        }
        else if (sender instanceof ConsoleCommandSender)
        {
            return consoleGroup;
        }
        else
        {
            return getGroupByName("default");
        }
    }

    public Component getColoredName(CommandSender sender)
    {
        Objects.requireNonNull(sender);
        return Component.text(sender.getName()).color(getSenderGroup(sender).getColor());
    }

    @Getter
    public static class Group
    {
        private final String internalName;
        private final String name;
        private final Component displayName;
        private final TextColor color;

        public static Group fromConfigurationSection(String internalName, ConfigurationSection section)
        {
            String name = section.getString("name", internalName);
            TextColor color;
            if (section.isString("color"))
            {
                TextColor temp = NamedTextColor.NAMES.value(Objects.requireNonNull(section.getString("color")).toLowerCase());

                // UGLY HACK COMING RIGHT UP
                if (temp == null)
                {
                    try
                    {
                        color = TextColor.color(Color.decode(Objects.requireNonNull(section.getString("color"))).getRGB());
                    }
                    catch (NumberFormatException ex)
                    {
                        color = NamedTextColor.GRAY;
                    }
                }
                else
                {
                    color = temp;
                }
            }
            else
            {
                color = TextColor.color(Math.max(Math.min(section.getInt("color", NamedTextColor.GRAY.value()), 0xFFFFFF), 0));
            }

            Component displayName = section.getRichMessage("displayName", Component.text(name).color(color));

            return new Group(internalName, name, displayName, color);
        }

        private Group(String internalName, String name, TextColor textColor)
        {
            this.internalName = internalName;
            this.name = name;
            this.displayName = Component.text(name).color(textColor);
            this.color = textColor;
        }

        private Group(String internalName, String name, Component displayName, TextColor color)
        {
            this.internalName = internalName;
            this.name = name;
            this.displayName = displayName;
            this.color = color;
        }
    }
}