package eu.koolfreedom.bridge;

import eu.koolfreedom.KoolSMPCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public interface DiscordIntegration<T> extends Listener
{
	GroupManagement.Group discordGroup = GroupManagement.Group.createGroup("discord", "Discord",
			Component.text("Discord").color(TextColor.color(0x5865F2)), TextColor.color(0x5865F2));
	Map<String, GroupManagement.Group> roleMap = new HashMap<>();

	T getDiscord();

	default DiscordIntegration<T> register()
	{
		Bukkit.getPluginManager().registerEvents(this, KoolSMPCore.getInstance());
		return this;
	}

	boolean channelExists(String name);
}
