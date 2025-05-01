package eu.koolfreedom.command.impl;

import org.bukkit.ChatColor;

@SuppressWarnings("deprecation")
public class Messages
{
    static final String MSG_NO_PERMS = ChatColor.RED + "I'm sorry but you do not have permission to perform this command. Please contact the server administrator if you believe that this is in error.";
    static final String RELOADED = ChatColor.GRAY + "The configuration file was successfully reloaded!";
    static final String FAILED = ChatColor.RED + "The configuration file failed to reload.";
    static final String PLAYER_NOT_FOUND = "<gray>Could not find the specified player on the server (are they online?)";
    static final String ONLY_IN_GAME = ChatColor.RED + "Only players can use this command";
    static final String INVALID_REASON = ChatColor.RED + "You did not provide a valid reason.";
    static final String MISSING_ARGS = ChatColor.GRAY + "Please provide a message.";
    static final String CMDSPY_ENABLED = ChatColor.GREEN + "CommandSpy has been enabled";
    static final String CMDSPY_DISABLED = ChatColor.RED + "CommandSpy has been disabled";
    static final String NO_REASON = ChatColor.RED + "No reason provided.";
}
