package eu.koolfreedom.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public abstract class BaseCommand implements CommandExecutor, TabCompleter {
    private final String name;

    public BaseCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Bukkit.getLogger().info("[KoolSMPCore] " + label + " command is being executed by " + sender.getName());

        if (sender instanceof Player) {
            execute((Player) sender, args);
        } else {
            sender.sendMessage("This command can only be executed by a player.");
        }
        return true;
    }


    public abstract void execute(Player player, String[] args);
}
