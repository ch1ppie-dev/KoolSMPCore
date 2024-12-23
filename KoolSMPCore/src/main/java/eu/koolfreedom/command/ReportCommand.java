package eu.koolfreedom.command;

import eu.koolfreedom.KoolSMPCore;
import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ReportCommand implements CommandExecutor {
    private final ArrayList<Player> antispam = new ArrayList<>();

    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player;
        if (commandSender instanceof Player) {
            player = (Player)commandSender;
        } else {
            commandSender.sendMessage((Component)Component.text("Only players can use this command.", (TextColor)NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage((Component)Component.text("Usage: /" + s + " <player> <reason>", (TextColor)NamedTextColor.RED));
            return true;
        }
        Player playerArg = Bukkit.getServer().getPlayer(args[0]);
        if (playerArg == null) {
            player.sendMessage((Component)Component.text("Could not find the specified player on the server.", (TextColor)NamedTextColor.RED));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage((Component)Component.text("You did not provide a valid reason.", (TextColor)NamedTextColor.RED));
            return true;
        }
        String reason = StringUtils.join((Object[])args, " ", 1, args.length).replace("\\n", "").replace("`", "");
        if (reason.isBlank()) {
            player.sendMessage((Component)Component.text("You did not provide a valid reason.", (TextColor)NamedTextColor.RED));
            return true;
        }
        if (!this.antispam.contains(player)) {
            this.antispam.add(player);
            Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)KoolSMPCore.main, () -> this.antispam.remove(player), 60L);
        } else {
            player.sendMessage((Component)Component.text("You're only allowed to execute that command every 3 seconds.", (TextColor)NamedTextColor.RED));
            return true;
        }
        player.sendMessage(((TextComponent)Component.newline().append((Component)Component.text("Successfully submitted the report to staff.", (TextColor)NamedTextColor.GREEN))).appendNewline());
        if (player.getName().equals(playerArg.getName()))
            player.sendMessage((Component)Component.text("Also, why would you report yourself?", (TextColor)NamedTextColor.RED));
        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "discord bcast #1320586156135551026 ```Received report from " + player
                .getName() + " reporting " + playerArg.getName() + " for " + reason + "```");
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (!players.hasPermission("kf.admin"))
                continue;
            players.sendMessage(((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.newline()
                    .append((Component)Component.text("[", (TextColor)NamedTextColor.DARK_GRAY)))
                    .append((Component)Component.text("Staff", (TextColor)NamedTextColor.RED)))
                    .append((Component)Component.text("]", (TextColor)NamedTextColor.DARK_GRAY)))
                    .append((Component)Component.text(" Received report from ", (TextColor)NamedTextColor.GREEN)))
                    .append((Component)Component.text(player.getName(), (TextColor)NamedTextColor.GOLD)))
                    .append((Component)Component.text(" reporting ", (TextColor)NamedTextColor.GREEN)))
                    .append((Component)Component.text(playerArg.getName(), (TextColor)NamedTextColor.GOLD)))
                    .append((Component)Component.text(" for ", (TextColor)NamedTextColor.GREEN)))
                    .append((Component)Component.text(reason, (TextColor)NamedTextColor.GOLD)))
                    .appendNewline());
        }
        return true;
    }
}