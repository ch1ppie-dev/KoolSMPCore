package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.config.MainConfig;
import eu.koolfreedom.log.FLog;
import eu.koolfreedom.util.KoolSMPCoreBase;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class KoolSMPCoreCommand extends KoolCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0 || !sender.hasPermission("kf.senior"))
        {
            KoolSMPCore.BuildProperties build = KoolSMPCore.build;
            msg(sender, "<white><b>KoolSMPCore - The Core of KoolFreedom SMP.");
            msg(sender, "<gray>Version <white><version>.<build>", Placeholder.unparsed("version", build.version),
                    Placeholder.unparsed("build", build.number));
            msg(sender, "<gray>Compiled on <white><date></white> by <white><builder></white>.",
                    Placeholder.unparsed("date", build.date),
                    Placeholder.unparsed("builder", build.author));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload"))
        {
            try
            {
                MainConfig.load();
                msg(sender, "<green>The configuration file has been reloaded.");
            }
            catch (Exception ex)
            {
                FLog.error("Failed to load configuration", ex);
                msg(sender, "<red>An error occurred whilst attempting to reload the configuration.");
            }

            return true;
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args)
    {
        if (sender.hasPermission("kf.senior"))
        {
            return Collections.singletonList("reload");
        }
        else
        {
            return null;
        }
    }
}
