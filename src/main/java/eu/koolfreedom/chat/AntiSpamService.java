package eu.koolfreedom.chat;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.listener.MuteManager;
import eu.koolfreedom.util.FUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiSpamService implements Listener
{
    private static final int CHAT_MAX_PER_SEC      =  5;   // msgs‑per‑second before mute
    private static final int CMD_MAX_PER_SEC       =  5;   // cmds‑per‑second before kick
    private static final int WARN_AT_CHAT_MESSAGES = CHAT_MAX_PER_SEC / 2;
    private static final String BYPASS_PERMISSION  = "kfc.antispam.bypass";

    private final Map<UUID, Integer> chatCounts  = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> cmdCounts   = new ConcurrentHashMap<>();

    private final MuteManager muteManager;
    private final KoolSMPCore plugin;

    public AntiSpamService(KoolSMPCore plugin)
    {
        this.plugin = plugin;
        this.muteManager = plugin.getMuteManager();

        Bukkit.getPluginManager().registerEvents(this, plugin);

        /* reset counters once per second */
        new BukkitRunnable()
        {
            @Override public void run()
            {
                chatCounts.clear();
                cmdCounts.clear();
            }
        }.runTaskTimer(plugin, 20L, 20L);  // every 1s
    }

    /* ----------------------- CHAT ----------------------- */

    @EventHandler
    public void onChat(AsyncChatEvent e)
    {
        Player p = e.getPlayer();
        if (p.hasPermission(BYPASS_PERMISSION)) return;
        if (muteManager.isMuted(p))             return;   // already muted

        int count = chatCounts.merge(p.getUniqueId(), 1, Integer::sum);

        if (count > CHAT_MAX_PER_SEC)
        {
            muteManager.mute(p);                                   // permanent mute
            plugin.getAutoUndoManager().scheduleAutoUnmute(p);     // auto‑undo in 5min
            FUtil.staffAction(Bukkit.getConsoleSender(),
                    "Auto‑muted <player> for spamming chat",
                    Placeholder.unparsed("player", p.getName()));
            e.setCancelled(true);
            return;
        }

        if (count >= WARN_AT_CHAT_MESSAGES)
        {
            p.sendMessage(FUtil.miniMessage("<gray>Please slow down | spamming is not allowed."));
            e.setCancelled(true);
        }
    }

    /* -------------------- COMMANDS --------------------- */

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e)
    {
        Player p = e.getPlayer();
        if (p.hasPermission(BYPASS_PERMISSION)) return;

        int count = cmdCounts.merge(p.getUniqueId(), 1, Integer::sum);

        if (count > CMD_MAX_PER_SEC)
        {
            FUtil.staffAction(Bukkit.getConsoleSender(),
                    "Auto‑kicked <player> for spamming commands",
                    Placeholder.unparsed("player", p.getName()));
            p.kick(FUtil.miniMessage("<red>You were kicked for spamming commands."));
            e.setCancelled(true);
        }
    }
}
