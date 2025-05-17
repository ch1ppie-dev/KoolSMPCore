package eu.koolfreedom.event;

import eu.koolfreedom.reporting.Report;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class PlayerReportEvent extends Event
{
	@Getter
	private static final HandlerList handlerList = new HandlerList();

	private final @NonNull Player reporter;
	private final @NonNull OfflinePlayer reported;
	private final @NonNull String reason;
	private final @NonNull Report report;

	@Override
	public @NotNull HandlerList getHandlers()
	{
		return handlerList;
	}
}
