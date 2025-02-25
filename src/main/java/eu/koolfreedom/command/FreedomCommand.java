package eu.koolfreedom.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public abstract class FreedomCommand implements CommandExecutor
{
    private static final Map<String, FreedomCommand> commands = new HashMap<>();

    public static FreedomCommand getFrom(Command command)
    {
        return commands.get(command.getName().toLowerCase());
    }

    public abstract boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        Player player = sender instanceof Player ? (Player) sender : null;
        return run(sender, player, cmd, commandLabel, args, sender instanceof org.bukkit.command.ConsoleCommandSender);
    }

    public void register(String name)
    {
        commands.put(name.toLowerCase(), this);
    }
}