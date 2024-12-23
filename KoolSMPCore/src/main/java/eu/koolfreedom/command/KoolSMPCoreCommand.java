package eu.koolfreedom.command;

import eu.koolfreedom.KoolSMPCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
            commandSender.sendMessage((Component)Component.text("Only players can use this command.", (TextColor)NamedTextColor.RED));
            return true;
        }
        player.sendMessage(((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.text("KoolSMPCore v", (TextColor)NamedTextColor.GOLD)
                    .append((Component)Component.text(KoolSMPCore.main.getPluginMeta().getVersion(), (TextColor)NamedTextColor.GOLD)))
                    .append((Component)Component.text(" for the ", (TextColor)NamedTextColor.RED)))
                    .append((Component)Component.text("KoolFreedom SMP", (TextColor)NamedTextColor.GOLD)))
                    .append((Component)Component.text(" server", (TextColor)NamedTextColor.RED)))
                    .append((Component)Component.text(" by ", (TextColor)NamedTextColor.RED)))
                    .append((Component)Component.text("gamingto12", (TextColor)NamedTextColor.GOLD)));
        }
        return true;
    }
}