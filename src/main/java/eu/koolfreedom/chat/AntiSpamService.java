package eu.koolfreedom.chat;

import eu.koolfreedom.KoolSMPCore;
import eu.koolfreedom.util.FUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiSpamService implements Listener
{

    /* ---------- config ---------- */
    private static final int MSG_PER_CYCLE       = 8;    // chat msgs / 1s
    private static final int COMMANDS_PER_CYCLE  = 5;    // commands / 1s
    private static final int MUTE_MINUTES_STEP   = 2;    // each spam = +2 mins mute
    private static final int CYCLE_TICKS         = 20;   // 1 second
    /* ---------------------------- */

    private final Map<UUID, Integer> chatCount    = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> cmdCount     = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> muteStrikes  = new ConcurrentHashMap<>();

    private final KoolSMPCore plugin;

    public AntiSpamService(KoolSMPCore plugin)
    {
        this.plugin = plugin;

        /* reset counters every second */
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                chatCount.clear();
                cmdCount.clear();
            }
        }.runTaskTimer(plugin, CYCLE_TICKS, CYCLE_TICKS);

        /* register events */
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /* ===== chat handler ===== */
    @EventHandler(priority = EventPriority.LOW)
    @SuppressWarnings("deprecation")
    public void onChat(AsyncPlayerChatEvent e)
    {
        Player p = e.getPlayer();
        if (p.hasPermission("kfc.antispam.bypass")) return;

        int total = chatCount.merge(p.getUniqueId(), 1, Integer::sum);
        if (total <= MSG_PER_CYCLE) return;              // under limit

        /* ----- over chat limit → punish ----- */
        e.setCancelled(true);

        int strikes     = muteStrikes.merge(p.getUniqueId(), 1, Integer::sum);
        int muteMinutes = strikes * MUTE_MINUTES_STEP;

        /* 1)  MUTE NOW  */
        plugin.getMuteManager().setMuted(p, true);

        /* 2)  SCHEDULE AUTOMATIC UNMUTE  */
        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    if (p.isOnline())
                    {
                        plugin.getMuteManager().setMuted(p, false);
                    }

                },
                muteMinutes * 60L * 20L    // minutes → ticks
        );

        FUtil.broadcast("<red><player> was muted for spamming.", Placeholder.unparsed("player", p.getName()));
    }

    /* ===== command handler ===== */
    @EventHandler(priority = EventPriority.LOW)
    public void onCommand(PlayerCommandPreprocessEvent e)
    {
        Player p = e.getPlayer();
        if (p.hasPermission("kfc.antispam.bypass")) return;

        int total = cmdCount.merge(p.getUniqueId(), 1, Integer::sum);
        if (total <= COMMANDS_PER_CYCLE) return;

        e.setCancelled(true);
        p.kick(FUtil.miniMessage("<red>Kicked for spamming commands."));
        FUtil.broadcast("<red><player> was kicked for spamming commands.", Placeholder.unparsed("player", p.getName()));
    }
}
