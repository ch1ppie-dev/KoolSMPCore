package eu.koolfreedom.command.impl;

import eu.koolfreedom.command.CommandParameters;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.config.ConfigEntry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandParameters(name = "admininfo", description = "Displays info about applying for staff", aliases = {"ai", "staffinfo",})
public class AdminInfoCommand extends KoolCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String label, String[] args)
    {
        String website_or_forums = ConfigEntry.SERVER_WEBSITE_OR_FORUM.getString();
        if (playerSender == null)
        {
            msg(sender, playersOnly);
        }

        msg(sender, "<red>To apply for a staff rank, head over to <gold>" + website_or_forums + "</gold> and fill out a staff application.");
        msg(sender, "<red>Be mindful of the rules and guidelines, and don't spam the staff team.");
        msg(sender, "<red>Failure to comply with the rules and guidelines will result in a consequence decided by staff.");
        return true;
    }
}
