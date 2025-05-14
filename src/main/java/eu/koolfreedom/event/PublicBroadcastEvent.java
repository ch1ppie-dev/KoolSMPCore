package eu.koolfreedom.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class PublicBroadcastEvent extends Event
{
	@Getter
	private static final HandlerList handlerList = new HandlerList();

	private final @NonNull Component message;

	@Override
	public @NotNull HandlerList getHandlers()
	{
		return handlerList;
	}
}
