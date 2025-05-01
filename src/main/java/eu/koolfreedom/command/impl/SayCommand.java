package eu.koolfreedom.command.impl;

import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.util.FUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SayCommand extends KoolCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0)
        {
            return false;
        }

        FUtil.broadcast(ConfigEntry.FORMATS_SAY.getString(), Placeholder.unparsed("name", sender.getName()),
                Placeholder.unparsed("message", StringUtils.join(args, " ")));

        // TODO: NO
        // This will only execute if there is DiscordSRV installed, I will not make this a dependency, but be warned
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast " + (String.format("[Server:%s] %s", sender.getName(), StringUtils.join(args, " "))));
        return true;
    }
}