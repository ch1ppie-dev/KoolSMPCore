package eu.koolfreedom.event;

import eu.koolfreedom.reporting.Report;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class PlayerReportDeleteEvent extends Event
{
	@Getter
	private static final HandlerList handlerList = new HandlerList();

	private final @NonNull Component staffDisplayName;
	private final @NonNull String staffName;
	private final @NonNull String staffId;
	private final @NonNull List<Report> reports;

	public PlayerReportDeleteEvent(boolean async, @NonNull Component staffDisplayName, @NonNull String staffName,
								   @NonNull String staffId, @NonNull List<Report> reports)
	{
		super(async);
		this.staffDisplayName = staffDisplayName;
		this.staffName = staffName;
		this.staffId = staffId;
		this.reports = reports;
	}

	@Override
	public @NotNull HandlerList getHandlers()
	{
		return handlerList;
	}
}
