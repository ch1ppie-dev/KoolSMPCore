package eu.koolfreedom.command;

import eu.koolfreedom.KoolSMPCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class KoolCommand implements TabExecutor
{
    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, @NotNull String[] args)
    {
        try
        {
            if (!run(sender, sender instanceof ConsoleCommandSender ? null : (Player) sender, cmd, commandLabel, args, sender instanceof ConsoleCommandSender))
            {
                msg(sender, "<gray>Usage: <usage>", Placeholder.component("usage",
                        KoolSMPCore.main.mmDeserialize(cmd.getUsage(), Placeholder.unparsed("command", commandLabel))));
                return true;
            }
        }
        catch (Throwable ex)
        {
            msg(sender, Component.text(ex.getMessage()).color(NamedTextColor.RED));
        }
        return false;
    }

    @Override
    public final @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, @NotNull String[] args)
    {
        final List<String> entries = tabComplete(sender, cmd, commandLabel, args);

        if (entries == null || entries.isEmpty() || args.length == 0)
        {
            return null;
        }

        return entries.stream().filter(entry -> entry.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
    }

    public abstract boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole);

    public List<String> tabComplete(CommandSender sender, Command command, String commandLabel, String[] args)
    {
        return List.of();
    }

    protected void msg(CommandSender sender, String message, TagResolver... placeholders)
    {
        msg(sender, KoolSMPCore.main.mmDeserialize(message, placeholders));
    }

    protected void msg(CommandSender sender, Component message)
    {
        sender.sendMessage(message);
    }
}