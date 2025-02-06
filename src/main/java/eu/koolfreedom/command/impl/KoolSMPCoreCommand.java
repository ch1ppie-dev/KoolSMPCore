package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KoolSMPCoreCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player;
        if (commandSender instanceof Player) {
            player = (Player)commandSender;
        } else {
            commandSender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }
        if (args.length > 0 && (args[0].equalsIgnoreCase("reloadconfig") || args[0].equalsIgnoreCase("reload"))) {
            if (!player.hasPermission("kf.senior")) {
                player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                return true;
            }
            KoolSMPCore.main.reloadConfig();
            player.sendMessage(Component.text("Successfully reloaded the configuration.", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("KoolSMPCore v", NamedTextColor.GOLD)
                    .append(Component.text(KoolSMPCore.main.getPluginMeta().getVersion(), NamedTextColor.GOLD))
                    .append(Component.text(" for the ", NamedTextColor.RED))
                    .append(Component.text("KoolFreedom SMP", NamedTextColor.GOLD))
                    .append(Component.text(" server", NamedTextColor.RED))
                    .append(Component.text(" by ", NamedTextColor.RED))
                    .append(Component.text("gamingto12", NamedTextColor.GOLD)));
        }
        return true;
    }
}