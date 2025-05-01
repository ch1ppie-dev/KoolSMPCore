package eu.koolfreedom.api;

import java.util.*;

import eu.koolfreedom.util.KoolSMPCoreBase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class Permissions extends KoolSMPCoreBase
{
    public Group getSenderGroup(CommandSender sender)
    {
        if (sender instanceof Player player)
        {
            return Group.getByName(Objects.requireNonNull(api.getUserManager().getUser(player.getUniqueId())).getPrimaryGroup());
        }
        else if (sender instanceof ConsoleCommandSender)
        {
            return Group.CONSOLE;
        }
        else
        {
            return Group.DEFAULT;
        }
    }

    public Component getColoredName(CommandSender sender)
    {
        Objects.requireNonNull(sender);

        return Component.text(sender.getName()).color(getSenderGroup(sender).getColor());
    }

    // are we gaming or are we gaming
    // yes we are
    @Getter
    @RequiredArgsConstructor
	public enum Group
    {
        DEFAULT("Member", NamedTextColor.GRAY),
        MOD("Moderator", NamedTextColor.GREEN),
        ADMIN("Admin", NamedTextColor.AQUA),
        SENIOR("Senior", NamedTextColor.GOLD),
        DEVELOPER("Developer", NamedTextColor.DARK_PURPLE),
        MANAGER("Manager", NamedTextColor.LIGHT_PURPLE),
        CO_OWNER("Co-Owner", NamedTextColor.RED),
        EXECUTIVE("Executive", NamedTextColor.DARK_RED),
        OWNER("Owner", NamedTextColor.RED),
        CONSOLE("Console", NamedTextColor.BLUE);

        private final String name;
        private final TextColor color;

        public Component getColoredName()
        {
            return Component.text(name).color(color);
        }

        public static Group getByName(String name)
        {
            return Arrays.stream(values()).filter(group -> group.getName().equalsIgnoreCase(name)).findAny()
                    .orElse(DEFAULT);
        }
    }
}