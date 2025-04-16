/*
/
/ Idea for the creation of this file comes from TotalFreedomMod and adjacent projects
/
/ We meant for this to be a 1:1 creation minus a few things,
/  but we wanted ot be as accurate as possible so that it functions the same way.
/
*/

package eu.koolfreedom.config;

import java.util.List;

public enum ConfigEntry
{
    SERVER_WEBSITE_OR_FORUM(String.class, "server.website_or_forum"),
    DISCORD_BOT_TOKEN(String.class, "discord.bot_token"),
    DISCORD_STAFF_ACTION_CHANNEL_ID(String.class, "discord.staff_action_channel_id"),
    UNBANNABLE_USERNAMES(List.class, "unbannable_usernames"),
    DISCORD_REPORT_CHANNEL_ID(String.class, "discord.report_channel_id");

    private final Class<?> type;
    private final String configName;

    ConfigEntry(Class<?> type, String configName) {
        this.type = type;
        this.configName = configName;
    }

    public Class<?> getType()
    {
        return type;
    }

    public String getConfigName()
    {
        return configName;
    }

    public String getString()
    {
        return MainConfig.getString(this);
    }

    public String setString(String value)
    {
        MainConfig.setString(this, value);
        return value;
    }

    public Double getDouble()
    {
        return MainConfig.getDouble(this);
    }

    public Double setDouble(Double value)
    {
        MainConfig.setDouble(this, value);
        return value;
    }

    public Boolean getBoolean()
    {
        return MainConfig.getBoolean(this);
    }

    public Boolean setBoolean(Boolean value)
    {
        MainConfig.setBoolean(this, value);
        return value;
    }

    public Integer getInteger()
    {
        return MainConfig.getInteger(this);
    }

    public Integer setInteger(Integer value)
    {
        MainConfig.setInteger(this, value);
        return value;
    }

    public List<?> getList()
    {
        return MainConfig.getList(this);
    }

    public static ConfigEntry findConfigEntry(String name)
    {
        name = name.toLowerCase().replace("_", "");
        for (ConfigEntry entry : values())
        {
            if (entry.toString().toLowerCase().replace("_", "").equals(name))
            {
                return entry;
            }
        }
        return null;
    }
}

