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

        msg(sender, "<gold>To apply for a staff rank, head over to <red>" + website_or_forums + "</red> and fill out a staff application.");
        msg(sender, "<gold>Be mindful of the <red>rules and guidelines</red>, and don't spam the staff team.");
        msg(sender, "<gold>Failure to comply with the rules and guidelines will result in a <b><red>ban</red></b>.");
        msg(sender, "<gold>Do not bug staff members to look at your application or else it will most likely get <red>denied</red>.");
        return true;
    }
}
