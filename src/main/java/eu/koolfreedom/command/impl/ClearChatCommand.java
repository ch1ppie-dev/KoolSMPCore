package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.util.FUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClearChatCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(KoolSMPCore.main, () -> {
            for (Player players : Bukkit.getOnlinePlayers()) {
                for (int i = 0; i < 2500; i++)
                    players.sendMessage(Component.text(getRandomColorCode().repeat(KoolSMPCore.random.nextInt(1, 12))));
                FUtil.adminAction(sender.getName(), "Cleared the chat", true);
                players.sendMessage(Component.text(""));
            }
        });
        return true;
    }

    private String getRandomColorCode() {
        char[] colors = "0123456789abcdefklmnor".toCharArray();
        return "§" + colors[KoolSMPCore.random.nextInt(colors.length)];
    }
}