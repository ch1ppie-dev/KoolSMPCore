package eu.koolfreedom.listener;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.command.impl.MuteChatCommand;
import eu.koolfreedom.util.FUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MuteManager implements Listener
{
    private final List<UUID> muted = new ArrayList<>();
    private final List<UUID> blockedCommands = new ArrayList<>();

    public MuteManager()
    {
        Bukkit.getPluginManager().registerEvents(this, KoolSMPCore.getInstance());
    }

    public void setMuted(Player player, boolean mute)
    {
        if (mute)
        {
            muted.add(player.getUniqueId());
        }
        else
        {
            muted.remove(player.getUniqueId());
        }
    }

    public int wipeMutes()
    {
        int amount = muted.size();
        muted.clear();
        return amount;
    }

    public int getMuteCount()
    {
        return muted.size();
    }

    public boolean isMuted(Player player)
    {
        return muted.contains(player.getUniqueId());
    }

    public void setCommandsBlocked(Player player, boolean block)
    {
        if (block)
        {
            blockedCommands.add(player.getUniqueId());
        }
        else
        {
            blockedCommands.remove(player.getUniqueId());
        }
    }

    public boolean isCommandsBlocked(Player player)
    {
        return blockedCommands.contains(player.getUniqueId());
    }

    public int wipeBlockedCommands()
    {
        int amount = blockedCommands.size();
        blockedCommands.clear();
        return amount;
    }


    @EventHandler
    public void onPlayerChat(AsyncChatEvent event)
    {
        Player player = event.getPlayer();

        if (isMuted(player))
        {
            player.sendMessage(FUtil.miniMessage("<gray>You are currently muted."));
            event.setCancelled(true);
        }
    }

    // Fallback in case AsyncChatEvent doesn't block it effectively enough
    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();

        if (isMuted(player))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();

        if (isCommandsBlocked(player))
        {
            event.setCancelled(true);
            player.sendMessage(FUtil.miniMessage("<red>You are not allowed to use commands right now."));
            return;
        }

        if (isMuted(player))
        {
            event.setCancelled(true);
            player.sendMessage(FUtil.miniMessage("<red>You are muted, you cannot use commands."));
        }

        if (player.isFrozen())
        {
            event.setCancelled(true);
            player.sendMessage(FUtil.miniMessage("<red>You are frozen, you cannot use commands."));
        }
    }

    // this should only be used in the mutechat command
    @EventHandler
    @SuppressWarnings("deprecation")
    public void muteChatEvent(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();

        if (MuteChatCommand.isChatMuted() && !player.hasPermission("kfc.mutechat.bypass"))
        {
            event.setCancelled(true);
            player.sendMessage(FUtil.miniMessage("<red>Chat is currently muted, you cannot speak."));
        }
    }

    // half-assed way to make sure players are still muted if the muted player relogs
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (isMuted(player))
        {
            setMuted(player, true);
        }
    }
}