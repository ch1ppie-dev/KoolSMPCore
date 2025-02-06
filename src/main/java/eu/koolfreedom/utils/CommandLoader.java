package eu.koolfreedom.utils;

import eu.koolfreedom.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.Set;

public class CommandLoader {
    private final Plugin plugin;
    private CommandMap commandMap;

    public CommandLoader(Plugin plugin) {
        this.plugin = plugin;
        getCommandMap();
    }

    private void getCommandMap() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            this.commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerCommands(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends BaseCommand>> commandClasses = reflections.getSubTypesOf(BaseCommand.class);

        for (Class<? extends BaseCommand> clazz : commandClasses) {
            try {
                BaseCommand commandInstance = clazz.getDeclaredConstructor().newInstance();
                registerCommand(commandInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void registerCommand(BaseCommand command) {
        if (commandMap != null) {
            Command bukkitCommand = new BukkitCommand(command.getName()) {
                @Override
                public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                    return command.onCommand(sender, this, commandLabel, args);
                }
            };

            commandMap.register(plugin.getName(), bukkitCommand);
            Bukkit.getLogger().info("[KoolSMPCore] Registered command: " + command.getName());
        } else {
            Bukkit.getLogger().warning("[KoolSMPCore] CommandMap is null! Commands cannot be registered.");
        }
    }
}
