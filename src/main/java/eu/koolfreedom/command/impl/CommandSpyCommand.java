package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.config.ConfigEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;

public class CommandSpyCommand extends KoolCommand implements Listener
{
    private final List<UUID> commandSpyEnabled = new ArrayList<>();

    public CommandSpyCommand()
    {
        Bukkit.getPluginManager().registerEvents(this, KoolSMPCore.getInstance());
    }

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args)
    {
        if (playerSender == null)
        {
            msg(sender, playersOnly);
            return true;
        }

        if (commandSpyEnabled.contains(playerSender.getUniqueId()))
        {
            commandSpyEnabled.remove(playerSender.getUniqueId());
            msg(sender, "<gray>CommandSpy <green>enabled</green>.");
        }
        else
        {
            commandSpyEnabled.add(playerSender.getUniqueId());
            msg(sender, "<gray>CommandSpy <green>enabled</green>.");
        }

        return true;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event)
    {
        Player sender = event.getPlayer();
        String commandMessage = event.getMessage();

        Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("kf.admin"))
                .filter(player -> commandSpyEnabled.contains(player.getUniqueId()))
                .forEach(player -> msg(player, ConfigEntry.FORMATS_COMMANDSPY.getString(),
                        Placeholder.parsed("name", sender.getName()),
                        Placeholder.parsed("raw_command", commandMessage),
                        Placeholder.component("command", Component.text(commandMessage))));
    }
}
