package eu.koolfreedom.bridge.vanish;

import de.myzelyam.supervanish.SuperVanish;
import eu.koolfreedom.bridge.VanishIntegration;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class SuperVanishIntegration implements VanishIntegration<SuperVanish>
{
	private final SuperVanish plugin;

	public SuperVanishIntegration()
	{
		plugin = SuperVanish.getPlugin(SuperVanish.class);
	}

	@Override
	public boolean isVanished(Player player)
	{
		return plugin.getVanishPlayer(player).isOnlineVanished();
	}
}
