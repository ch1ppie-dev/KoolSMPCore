package eu.koolfreedom.command;

import eu.koolfreedom.log.FLog;
import eu.koolfreedom.util.FUtil;
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
    protected final String playerNotFound = "<gray>Could not find the specified player on the server (are they online?)";
    protected final String playersOnly = "<red>This command can only be executed in-game.";

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, @NotNull String[] args)
    {
        try
        {
            if (!run(sender, sender instanceof Player player ? player : null, cmd, commandLabel, args))
            {
                msg(sender, "<gray>Usage: <usage>", Placeholder.component("usage",
                        FUtil.miniMessage(cmd.getUsage(), Placeholder.unparsed("command", commandLabel))));
            }
        }
        catch (Throwable ex)
        {
            msg(sender, Component.text(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName()).color(NamedTextColor.RED));
            FLog.error("Command error", ex);
        }

        return true;
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

    public abstract boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args);

    public List<String> tabComplete(CommandSender sender, Command command, String commandLabel, String[] args)
    {
        return List.of();
    }

    protected final void msg(CommandSender sender, String message, TagResolver... placeholders)
    {
        sender.sendRichMessage(message, placeholders);
    }

    protected final void msg(CommandSender sender, Component message)
    {
        sender.sendMessage(message);
    }

    protected final void broadcast(Component messsge)
    {
        FUtil.broadcast(messsge);
    }

    protected final void broadcast(Component messsge, String permission)
    {
        FUtil.broadcast(messsge, permission);
    }

    protected final void broadcast(String message, TagResolver... placeholders)
    {
        FUtil.broadcast(message, placeholders);
    }

    protected final void broadcast(String permission, String message, TagResolver... placeholders)
    {
        FUtil.broadcast(permission, message, placeholders);
    }
}