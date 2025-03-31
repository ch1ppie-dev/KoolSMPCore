package eu.koolfreedom.config;

import eu.koolfreedom.KoolSMPCore;

public enum ConfigEntry
{
    SERVER_WEBSITE_OR_FORUM("server.website_or_forum");

    private final String path;

    ConfigEntry(String path)
    {
        this.path = path;
    }

    private static Config config = KoolSMPCore.getPlugin().config;

    public String getString()
    {
        return config.getString(path);
    }
}

