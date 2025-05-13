package eu.koolfreedom.bridge;

import org.bukkit.entity.Player;

public interface VanishIntegration<T>
{
	T getPlugin();

	boolean isVanished(Player player);
}
