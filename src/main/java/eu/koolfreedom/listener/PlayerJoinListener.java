package eu.koolfreedom.listener;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.note.NoteManager;
import eu.koolfreedom.note.PlayerNote;
import eu.koolfreedom.util.FUtil;
import eu.koolfreedom.freeze.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.UUID;

public class PlayerJoinListener implements Listener
{
    private final FreezeManager freezeManager = KoolSMPCore.getInstance().getFreezeManager();
    private final MuteManager muteManager = KoolSMPCore.getInstance().getMuteManager();
    private final LockupManager lockupManager = KoolSMPCore.getInstance().getLockupManager();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        NoteManager noteManager = KoolSMPCore.getInstance().getNoteManager();

        List<PlayerNote> notes = noteManager.getNotes(player.getUniqueId());

        if (notes.isEmpty())
            return;

        for (Player online : Bukkit.getOnlinePlayers())
        {
            if (!online.hasPermission("kfc.admin")) continue;

            online.sendMessage(Component.text()
                    .append(Component.text("[Note] ", NamedTextColor.YELLOW))
                    .append(Component.text(player.getName(), NamedTextColor.RED))
                    .append(Component.text(" has ", NamedTextColor.YELLOW))
                    .append(Component.text(notes.size() + " staff note(s).", NamedTextColor.RED))
            );

            for (PlayerNote note : notes)
            {
                online.sendMessage(Component.text("- " + note.getMessage(), NamedTextColor.GRAY));
            }
        }

        if (freezeManager.isFrozen(player))
        {
            freezeManager.freeze(player);
            player.sendMessage(FUtil.miniMessage("<red>Just because you re-logged, doesn't mean you're safe"));
        }

        if (muteManager.isMuted(player))
        {
            player.sendMessage(FUtil.miniMessage("<gray>You are still muted."));
        }

        UUID id = player.getUniqueId();

        if (muteManager.isCommandsBlocked(id))
        {
            player.sendMessage(FUtil.miniMessage("<gray>Your commands are still blocked."));
        }

        if (lockupManager.isLocked(player.getUniqueId()))
        {
            lockupManager.lock(player);
            player.sendMessage(FUtil.miniMessage("<red>Just because you re-logged doesn't mean you're safe!"));
        }
    }

}
