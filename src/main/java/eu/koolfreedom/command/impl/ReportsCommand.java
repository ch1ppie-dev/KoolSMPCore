package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.reporting.Report;
import eu.koolfreedom.reporting.ReportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ReportsCommand extends KoolCommand
{
	@Override
	public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args)
	{
		if (args.length == 0)
		{
			return false;
		}

		final ReportManager reportManager = KoolSMPCore.getInstance().reportManager;

		switch (args[0].toLowerCase())
		{
			// Surface level, does not require a report ID to be specified
			case "recent" ->
			{
				final List<Report> reports = reportManager.getReports(true);

				if (reports.isEmpty())
				{
					msg(sender, "<green>There are no reports.");
				}
				else
				{
					msg(sender, "<gradient:dark_gray:gray>████</gradient> Recent Reports <gradient:gray:dark_gray>████</gradient>");
					reports.forEach(report -> msg(sender, report.summary()));
				}
			}
			case "unresolved" ->
			{
				final List<Report> unresolved = reportManager.getReports(false);

				if (unresolved.isEmpty())
				{
					msg(sender, "<green>There are no reports for you to resolve.");
				}
				else
				{
					msg(sender, "<gradient:dark_gray:gray>████</gradient>   Unresolved Reports   <gradient:gray:dark_gray>████</gradient>");
					unresolved.forEach(report -> msg(sender, report.summary()));
				}
			}

			// Requires additional parameters
			case "summary" ->
			{
				if (args.length != 2) return false;

				final Report workingReport = reportManager.getReport(args[1]);

				if (workingReport == null)
				{
					msg(sender, "<red>No report could be found with that ID.");
					return true;
				}

				msg(sender, workingReport.fullSummary());
			}
			case "reopen" ->
			{
				if (args.length < 2) return false;
				final Report report = reportManager.getReport(args[1]);
				final String reason = args.length >= 3 ? StringUtils.join(ArrayUtils.subarray(args, 2, args.length), " ") : "Pending further investigation";

				if (report == null)
				{
					msg(sender, "<red>No report could be found with that ID.");
					return true;
				}
				else if (!report.isResolved())
				{
					msg(sender, "<red>That report is already open.");
					return true;
				}

				report.update(playerSender != null ? playerSender.displayName() : Component.text(sender.getName()),
						sender.getName(),
						playerSender != null ? playerSender.getUniqueId().toString() : sender.getName(),
						Report.ReportStatus.REOPENED,
						reason);

				msg(sender, "<green>Report <dark_green><id></dark_green> has been re-opened.",
						Placeholder.unparsed("id", report.getId() != null ? report.getId() : ""));
			}
			case "handle" ->
			{
				if (args.length == 1) return false;
				final Report report = reportManager.getReport(args[1]);
				final String reason = args.length >= 3 ? StringUtils.join(ArrayUtils.subarray(args, 2, args.length), " ") : "Handled";

				if (report == null)
				{
					msg(sender, "<red>No report could be found with that ID.");
					return true;
				}
				else if (report.isResolved())
				{
					msg(sender, "<red>That report is already closed.");
					return true;
				}

				report.update(playerSender != null ? playerSender.displayName() : Component.text(sender.getName()),
						sender.getName(),
						playerSender != null ? playerSender.getUniqueId().toString() : sender.getName(),
						Report.ReportStatus.CLOSED,
						reason);

				msg(sender, "<green>Report <dark_green><id></dark_green> has been handled.",
						Placeholder.unparsed("id", report.getId() != null ? report.getId() : ""));
			}
			case "close" ->
			{
				if (args.length < 3) return false;
				final Report report = reportManager.getReport(args[1]);
				final String reason = StringUtils.join(ArrayUtils.subarray(args, 2, args.length), " ");

				if (report == null)
				{
					msg(sender, "<red>No report could be found with that ID.");
					return true;
				}
				else if (report.isResolved())
				{
					msg(sender, "<red>That report is already closed.");
					return true;
				}

				report.update(playerSender != null ? playerSender.displayName() : Component.text(sender.getName()),
						sender.getName(),
						playerSender != null ? playerSender.getUniqueId().toString() : sender.getName(),
						Report.ReportStatus.CLOSED,
						reason);

				msg(sender, "<green>Report <dark_green><id></dark_green> has been closed.",
						Placeholder.unparsed("id", report.getId() != null ? report.getId() : ""));
			}
			default ->
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		final List<String> acceptableModes = List.of("summary", "close", "handle", "reopen", "recent", "unresolved");

		final ReportManager reportManager = KoolSMPCore.getInstance().reportManager;

		// Show the available modes
		if (args.length == 1)
		{
			return acceptableModes;
		}
		// Tab complete for modes
		else if (acceptableModes.contains(args[0].toLowerCase()))
		{
			// Tab complete, but also give the summary of the report before the user executes it (if present)
			if (args.length == 2)
			{
				// "recent" and "unresolved" should only show an empty list
				if (!args[0].equalsIgnoreCase("recent") && !args[0].equalsIgnoreCase("unresolved"))
				{
					// Don't show the summary of the report unless the user intends to close, handle, or re-open a report
					boolean dontShowSummary = args[0].equalsIgnoreCase("summary");
					boolean reopeningReports = args[0].equalsIgnoreCase("reopen");

					if (!dontShowSummary)
					{
						Report report = reportManager.getReport(args[1]);

						if (report != null && (!report.isResolved() || reopeningReports))
						{
							msg(sender, report.fullSummary());
						}
					}

					return args[0].equalsIgnoreCase("reopen") ? reportManager.getClosedReportIDs()
							: reportManager.getReportIds(dontShowSummary);
				}
			}
			// Not for tab completions but shows the user the summary of the report before they execute it
			else if (args.length == 3)
			{
				if (args[0].equalsIgnoreCase("close") || args[0].equalsIgnoreCase("handle"))
				{
					Report report = reportManager.getReport(args[1]);

					if (report != null && !report.isResolved() && args[2].isBlank())
					{
						msg(sender, report.fullSummary());
					}
				}
			}
		}

		return List.of();
	}
}
