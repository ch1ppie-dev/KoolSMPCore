package eu.koolfreedom.event;

import eu.koolfreedom.reporting.Report;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class PlayerReportUpdateEvent extends Event
{
	@Getter
	private static final HandlerList handlerList = new HandlerList();

	private final @NonNull Component staffDisplayName;
	private final @NonNull String staffName;
	private final @NonNull String staffId;
	private final @NonNull Report.ReportStatus oldStatus;
	private final @NonNull Report.ReportStatus newStatus;
	private final @Nullable String newNote;
	private final @NonNull Report report;

	public PlayerReportUpdateEvent(boolean async, @NonNull Component staffDisplayName, @NonNull String staffName,
								   @NonNull String staffId, @NonNull Report.ReportStatus oldStatus,
								   @NonNull Report.ReportStatus newStatus, @Nullable String newNote,
								   @NonNull Report report)
	{
		super(async);
		this.staffDisplayName = staffDisplayName;
		this.staffName = staffName;
		this.staffId = staffId;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
		this.newNote = newNote;
		this.report = report;
	}

	@Override
	public @NotNull HandlerList getHandlers()
	{
		return handlerList;
	}
}
