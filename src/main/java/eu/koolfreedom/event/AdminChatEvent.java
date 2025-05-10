package eu.koolfreedom.event;

import eu.koolfreedom.api.GroupCosmetics;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Namespaced;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class AdminChatEvent extends Event
{
	@Getter
	private static final HandlerList handlerList = new HandlerList();

	private final CommandSender commandSender;
	private final Component senderDisplay;
	private final String senderName;
	private final GroupCosmetics.Group senderGroup;
	private final Component message;
	private final Namespaced source;

	public AdminChatEvent(boolean async, CommandSender commandSender, Component senderDisplay, String senderName,
						  GroupCosmetics.Group senderGroup, Component message, Namespaced source)
	{
		super(async);
		this.commandSender = commandSender;
		this.senderDisplay = senderDisplay;
		this.senderName = senderName;
		this.senderGroup = senderGroup;
		this.message = message;
		this.source = source;
	}

	@Override
	public @NotNull HandlerList getHandlers()
	{
		return handlerList;
	}
}
