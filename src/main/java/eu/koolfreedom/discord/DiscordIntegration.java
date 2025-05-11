package eu.koolfreedom.discord;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.api.GroupCosmetics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public interface DiscordIntegration<T> extends Listener
{
	GroupCosmetics.Group discordGroup = GroupCosmetics.Group.createGroup("discord", "Discord",
			Component.text("Discord").color(TextColor.color(0x5865F2)), TextColor.color(0x5865F2));
	Map<String, GroupCosmetics.Group> roleMap = new HashMap<>();

	T getDiscord();

	default DiscordIntegration<T> register()
	{
		Bukkit.getPluginManager().registerEvents(this, KoolSMPCore.getInstance());
		return this;
	}

	boolean channelExists(String name);
}
