package eu.koolfreedom;

import org.bukkit.plugin.java.*;
import org.bukkit.plugin.*;
import eu.koolfreedom.command.*;
import com.sk89q.worldguard.*;
import com.comphenix.protocol.*;
import net.kyori.adventure.text.*;
import eu.koolfreedom.listener.ExploitListener;
import net.kyori.adventure.text.format.*;
import java.time.*;
import net.kyori.adventure.title.*;
import java.util.regex.*;
import com.earth2me.essentials.*;
import org.bukkit.event.*;
import org.bukkit.potion.*;
import java.util.concurrent.*;
import net.kyori.adventure.text.serializer.legacy.*;
import org.bukkit.*;
import org.bukkit.command.*;
import java.net.*;
import com.sk89q.worldguard.protection.regions.*;
import java.util.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.*;

public class KoolSMPCore extends JavaPlugin implements Listener {
    public static KoolSMPCore main;

    @Override
    public void onEnable() {
        getLogger().info("KoolSMPCore is starting...");
        getLogger().info("KoolSMPCore has been enabled.");
        getServer().getPluginManager().registerEvents(this, this);

        Objects.requireNonNull(getCommand("clearchat")).setExecutor(new ClearChatCommand());
        Objects.requireNonNull(getCommand("report")).setExecutor(new ReportCommand());
        Objects.requireNonNull(getCommand("koolsmpcore")).setExecutor(new KoolSMPCoreCommand());
        Objects.requireNonNull(getCommand("lagsource")).setExecutor(new LagSourceCommand());
        Objects.requireNonNull(getCommand("spectate")).setExecutor(new SpectateCommand());
        Objects.requireNonNull(getCommand("obliterate")).setExecutor(new ObliterateCommand());
        Objects.requireNonNull(getCommand("doom")).setExecutor(new DoomCommand());
        Objects.requireNonNull(getCommand("hug")).setExecutor( new HugCommand());
        Objects.requireNonNull(getCommand("slap")).setExecutor(new SlapCommand());
        Objects.requireNonNull(getCommand("ship")).setExecutor(new ShipCommand());
        Objects.requireNonNull(getCommand("poke")).setExecutor(new PokeCommand());
        Objects.requireNonNull(getCommand("kiss")).setExecutor(new KissCommand());
        Objects.requireNonNull(getCommand("pat")).setExecutor(new PatCommand());
        Objects.requireNonNull(getCommand("crash")).setExecutor(new CrashCommand());


        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new ExploitListener(this));

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        if (getConfig().getBoolean("enable-announcer")) announcerRunnable();

        main = this;
    }

    @Override
    public void onDisable() {
        getLogger().info("KoolSMPCore has been disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    public boolean isVanished(Player player) {
        Plugin essentials = Bukkit.getServer().getPluginManager().getPlugin("Essentials");

        if (essentials == null) {
            return false;
        }

        return essentials.isEnabled() && ((Essentials) essentials).getUser(player).isVanished();
    }

    public boolean isInvisible(Player player) {
        return player.getGameMode() == GameMode.SPECTATOR || player.hasPotionEffect(PotionEffectType.INVISIBILITY) || isVanished(player);
    }

    private void announcerRunnable() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            List<String> messageKeys = new ArrayList<>(getConfig().getConfigurationSection("messages").getKeys(false));
            if (messageKeys.isEmpty()) {
                getLogger().warning("No messages found in configuration.");
                return;
            }
            String randomKey = messageKeys.get(ThreadLocalRandom.current().nextInt(messageKeys.size()));
            List<String> lines = getConfig().getStringList("messages." + randomKey);
            if (lines.isEmpty()) {
                getLogger().warning("Message '" + randomKey + "' has no lines.");
                return;
            }
            Bukkit.broadcast(Component.newline().append(LegacyComponentSerializer.legacyAmpersand().deserialize(String.join("\n", lines))).appendNewline());
        }, 0L, getConfig().getLong("announcer-time"));
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    private void onLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) return;

        if (getServer().getOnlinePlayers().size() >= getServer().getMaxPlayers()) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, Component.text("The server is full!"));
            return;
        }

        InetAddress address = event.getAddress();
        if (address == null) return; // Can be null according to bukkit

        int found = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            InetAddress addr = player.getAddress().getAddress();
            if (addr != null && addr.equals(address)) {
                found++;
                if (found >= 2) {
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("Too many connections from this IP address.", NamedTextColor.RED));
                    break;
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();

        if (message.startsWith("/") && !player.isOp()) {
            event.setCancelled(true);
            return;
        }

        if (player.hasPermission("kf.admin")) {
            if (message.contains("@everyone")) {
                String lastColor = ChatColor.getLastColors(message);
                message = message.replace("@everyone", "§b@everyone" + (lastColor.isEmpty() ? "§r" : lastColor));
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                }

                event.setMessage(message);
                return;
            }
        } else {
            if (message.trim().toLowerCase().contains("nigger") ||
                    (message.trim().toLowerCase().contains("nigga") ||
                            (message.trim().toLowerCase().contains("faggot")))) {
                event.setCancelled(true);

                player.sendMessage(String.format(event.getFormat(), player.getDisplayName(), message));

                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (player.isOnline()) {
                        getServer().dispatchCommand(Bukkit.getConsoleSender(), "obliterate " + player.getName() + " Racism");
                    } else {
                        getServer().dispatchCommand(Bukkit.getConsoleSender(), "banip " + player.getName() + " §c§lMay your worst nightmare come true, and may you suffer by the hands of your ruler, " + player.getName() + ". (Racism)");
                    }
                }, 50L);

                return;
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isVanished(p)) continue;

            if (message.contains("@" + p.getName())) {
                String lastColor = ChatColor.getLastColors(message);
                message = message.replace("@" + p.getName(), "§b@" + p.getName() + (lastColor.isEmpty() ? "§r" : lastColor));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
            }
        }
    }
    private record PotionEffectWithDuration(PotionEffect effect, int duration) {
    }
}
