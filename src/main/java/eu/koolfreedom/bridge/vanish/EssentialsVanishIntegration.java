package eu.koolfreedom.bridge.vanish;

import com.earth2me.essentials.Essentials;
import eu.koolfreedom.bridge.VanishIntegration;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class EssentialsVanishIntegration implements VanishIntegration<Essentials>
{
	private final Essentials plugin;

	public EssentialsVanishIntegration()
	{
		plugin = Essentials.getPlugin(Essentials.class);
	}

	@Override
	public boolean isVanished(Player player)
	{
		return plugin.getUser(player) != null && plugin.getUser(player).isVanished();
	}
}
