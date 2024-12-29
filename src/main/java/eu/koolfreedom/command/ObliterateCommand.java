package eu.koolfreedom.command;

import eu.koolfreedom.KoolSMPCore;
import java.util.Arrays;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutPosition;
import net.minecraft.world.entity.RelativeMovement;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ObliterateCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage((Component)Component.text("Usage: /" + s + " <player> [reason]", (TextColor)NamedTextColor.RED));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            commandSender.sendMessage((Component)Component.text("Could not find the specified player on the server.", (TextColor)NamedTextColor.RED));
            return true;
        }
        int i;
        for (i = 0; i < 30; i++)
            target.getWorld().strikeLightningEffect(target.getLocation());
        target.setFireTicks(200);
        target.setGameMode(GameMode.SURVIVAL);
        Bukkit.broadcast((Component)Component.text(commandSender.getName() + " is unleashing divine punishment upon " + commandSender.getName() + "!", (TextColor)NamedTextColor.RED));
        Bukkit.getScheduler().runTaskLater((Plugin)KoolSMPCore.main, () -> Bukkit.broadcast((Component)Component.text("May " + target.getName() + "'s soul be cleansed by the flames of destruction!", (TextColor)NamedTextColor.RED)), 2L);
        Bukkit.getScheduler().runTaskLater((Plugin)KoolSMPCore.main, () -> {
            Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "lp user " + target.getName() + " clear");
            if (target.isOp())
                target.setOp(false);
        },2L);
        Bukkit.getScheduler().runTaskLater((Plugin)KoolSMPCore.main, () -> target.setHealth(0.0D), 10L);
        Bukkit.getScheduler().runTaskLater((Plugin)KoolSMPCore.main, () -> Bukkit.broadcast((Component)Component.text(target.getName() + " has been eradicated from existence!", (TextColor)NamedTextColor.RED)), 30L);
        for (i = 0; i < 3; i++)
            Bukkit.getScheduler().runTaskLater((Plugin)KoolSMPCore.main, () -> Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "execute as \"" + target.getName() + "\" at @s run particle flame ~ ~ ~ 1 1 1 1 999999999 force @s"), 30L);
        Bukkit.getScheduler().runTaskLater((Plugin)KoolSMPCore.main, () -> (((CraftPlayer)target).getHandle()).c.b((Packet)new PacketPlayOutPosition(Double.POSITIVE_INFINITY, 0.0D, Double.POSITIVE_INFINITY, 90.0F, 0.0F, RelativeMovement.f, 0)), 30L);
        String reason = (args.length > 1) ? (" (" + String.join(" ", Arrays.<CharSequence>copyOfRange((CharSequence[])args, 1, args.length)) + ")") : "";
        Bukkit.getScheduler().runTaskLater((Plugin)KoolSMPCore.main, () -> Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "banip " + target.getName() + " &c&lMay your worst nightmare come true, and may you suffer by the hands of your ruler, " + target.getName() + "." + reason), 38L);
        return true;
    }
}