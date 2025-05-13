package eu.koolfreedom.command;

import eu.koolfreedom.log.FLog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CommandLoader
{
	private final List<KoolCommand> koolCommands = new ArrayList<>();

	public void loadCommands(List<Class<? extends KoolCommand>> commands)
	{
		commands.forEach(commandClass ->
		{
			try
			{
				final KoolCommand command = commandClass.getDeclaredConstructor().newInstance();
				Bukkit.getCommandMap().register(command.getName(), "koolsmpcore", command);
				koolCommands.add(command);
			}
			catch (Throwable ex)
			{
				FLog.error("Failed to load command {}", commandClass.getName(), ex);
			}
		});
	}
}
