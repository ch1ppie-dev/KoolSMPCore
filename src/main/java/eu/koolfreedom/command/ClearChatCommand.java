package eu.koolfreedom.command;

import eu.koolfreedom.KoolSMPCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ClearChatCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)KoolSMPCore.main, () -> {
            for (Player players : Bukkit.getOnlinePlayers()) {
                for (int i = 0; i < 2500; i++)
                    players.sendMessage((Component)Component.text(getRandomColorCode().repeat(KoolSMPCore.random.nextInt(1, 12))));
                players.sendMessage(Component.text("The chat has been cleared by ", (TextColor)NamedTextColor.RED).append((Component)Component.text(commandSender.getName(), (TextColor)NamedTextColor.GOLD)));
                players.sendMessage((Component)Component.text(""));
            }
        });
        return true;
    }

    private String getRandomColorCode() {
        char[] colors = "0123456789abcdefklmnor".toCharArray();
        return "&" + colors[KoolSMPCore.random.nextInt(colors.length)];
    }
}