/*
/
/ Idea for the creation of this file comes from TotalFreedomMod and adjacent projects
/
/ We meant for this to be a 1:1 creation minus a few things,
/  but we wanted ot be as accurate as possible so that it functions the same way.
/
*/

package eu.koolfreedom.config;

import eu.koolfreedom.KoolSMPCore;

public enum ConfigEntry
{
    SERVER_WEBSITE_OR_FORUM("server.website_or_forum"),
    DISCORD_BOT_TOKEN("discord.bot_token"),
    DISCORD_STAFF_ACTION_CHANNEL_ID("discord.staff_action_channel_id"),
    DISCORD_REPORT_CHANNEL_ID("discord.report_channel_id");

    private final String path;

    ConfigEntry(String path)
    {
        this.path = path;
    }

    private static final Config config = KoolSMPCore.getPlugin().config;

    public String getString()
    {
        return config.getString(path);
    }
}

