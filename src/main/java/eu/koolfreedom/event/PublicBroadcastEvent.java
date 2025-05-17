package eu.koolfreedom.event;

import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PublicBroadcastEvent extends Event
{
	@Getter
	private static final HandlerList handlerList = new HandlerList();

	private final @NonNull Component message;

	public PublicBroadcastEvent(boolean async, @NonNull Component message)
	{
		super(async);
		this.message = message;
	}

	@Override
	public @NotNull HandlerList getHandlers()
	{
		return handlerList;
	}
}
