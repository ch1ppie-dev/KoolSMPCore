package eu.koolfreedom.command.impl;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.command.CommandParameters;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.note.PlayerNote;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@CommandParameters(
        name = "note",
        description = "Add or view staff notes",
        usage = "/note <add|view> <player> [message]",
        aliases = {"notes"}
)
public class NoteCommand extends KoolCommand
{
    @Override
    public boolean run(CommandSender sender, Player player, Command cmd,
                       String label, String[] args)
    {
        if (args.length < 2)             // need at least sub‑cmd + player
            return false;

        String sub = args[0].toLowerCase();
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null)
        {
            msg(sender, playerNotFound);
            return true;
        }
        UUID uuid = target.getUniqueId();

        /* ========== /note add ========== */
        if (sub.equals("add"))
        {
            if (!sender.hasPermission("kfc.notes.add"))
            {
                msg(sender, "<red>You lack permission to add notes.");
                return true;
            }
            if (args.length < 3) return false;         // no message given

            String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            PlayerNote note = new PlayerNote(sender.getName(), message, LocalDateTime.now());
            KoolSMPCore.getInstance().getNoteManager().addNote(uuid, note);

            msg(sender, "<green>Note added for <yellow><player></yellow>.",
                    Placeholder.unparsed("player", target.getName()));
            return true;
        }

        /* ========== /note view ========== */
        if (sub.equals("view"))
        {
            if (!sender.hasPermission("kfc.notes.view"))
            {
                msg(sender, "<red>You lack permission to view notes.");
                return true;
            }

            var notes = KoolSMPCore.getInstance().getNoteManager().getNotes(uuid);
            if (notes.isEmpty())
            {
                msg(sender, "<gray>No notes for <player>.",
                        Placeholder.unparsed("player", target.getName()));
                return true;
            }

            msg(sender, "<gold>Notes for <yellow><player></yellow>:</gold>",
                    Placeholder.unparsed("player", target.getName()));
            notes.forEach(n ->
                    msg(sender,
                            "<gray>- <white>[<time>] <author>: <message></white>",
                            Placeholder.unparsed("time", n.getTimestamp().toString()),
                            Placeholder.unparsed("author", n.getAuthor()),
                            Placeholder.unparsed("message", n.getMessage())
                    ));
            return true;
        }

        /* unknown sub‑command */
        return false;
    }
}
