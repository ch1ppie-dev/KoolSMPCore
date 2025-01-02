package eu.koolfreedom;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import com.earth2me.essentials.Essentials;
import com.sk89q.worldguard.WorldGuard;
import eu.koolfreedom.command.ClearChatCommand;
import eu.koolfreedom.command.ReportCommand;
import eu.koolfreedom.command.KoolSMPCoreCommand;
import eu.koolfreedom.command.SpectateCommand;
import eu.koolfreedom.command.LagSourceCommand;
import eu.koolfreedom.command.ObliterateCommand;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.potion.PotionEffectType;
import eu.koolfreedom.system.CommandSpySystem;

public class KoolSMPCore extends JavaPlugin implements Listener {
    public static KoolSMPCore main;
    private CommandSpySystem commandSpySystem;
    private static RegionContainer container;
    public static final Random random = new Random();
    private final List<UUID> titleCooldown = new ArrayList<>();
    private static final List<String> BAN_COMMANDS = List.of("/ban", "/ban-ip", "/banip", "/ipban", "/tempban", "/tempbanip", "/tempipban", "/kick");

    @Override
    public void onEnable() {
        getLogger().info("Starting KoolSMPCore...");

        try {
            getLogger().info("Creating CommandSpySystem instance...");
            commandSpySystem = Objects.requireNonNull(new CommandSpySystem(this), "CommandSpySystem cannot be null");
            getLogger().info("CommandSpySystem instance created.");

            getLogger().info("Initializing CommandSpySystem...");
            commandSpySystem.initialize();
            getLogger().info("CommandSpySystem initialized.");
        } catch (Exception e) {
            getLogger().severe("Error during CommandSpySystem setup: " + e.getMessage());
            e.printStackTrace();
        }

        // Register plugin events
        getServer().getPluginManager().registerEvents(this, this);

        // Register other commands
        Objects.requireNonNull(getCommand("clearchat")).setExecutor(new ClearChatCommand());
        Objects.requireNonNull(getCommand("report")).setExecutor(new ReportCommand());
        Objects.requireNonNull(getCommand("koolsmpcore")).setExecutor(new KoolSMPCoreCommand());
        Objects.requireNonNull(getCommand("lagsource")).setExecutor(new LagSourceCommand());
        Objects.requireNonNull(getCommand("spectate")).setExecutor(new SpectateCommand());
        Objects.requireNonNull(getCommand("obliterate")).setExecutor(new ObliterateCommand());

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new ExploitListener(this));

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        if (getConfig().getBoolean("announcer-enabled")) {
            announcerRunnable();
        }

        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        main = this;

        getLogger().info("KoolSMPCore has been enabled.");
    }

    @Override
    public void onDisable() {
        if (commandSpySystem != null) {
            commandSpySystem.cleanup();
        }
        getLogger().info("KoolSMPCore has been disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }


    public boolean isVanished(Player player) {
        Plugin essentials = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
        if (essentials == null || !(essentials instanceof Essentials)) {
            return false;
        }
        return ((Essentials) essentials).getUser(player).isVanished();
    }

    public boolean isInvisible(Player player) {
        return player.getGameMode() == GameMode.SPECTATOR || player.hasPotionEffect(PotionEffectType.INVISIBILITY) || isVanished(player);
    }

    private void announcerRunnable() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this, () -> {
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
            Bukkit.broadcast(((TextComponent)Component.newline().append((Component)LegacyComponentSerializer.legacyAmpersand().deserialize(String.join("\n", (Iterable)lines)))).appendNewline());
        },0L,

                getConfig().getLong("announcer-delay"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }

        InetAddress address = event.getAddress();
        if (address == null) {
            return;
        }

        int connectionCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            InetAddress playerAddress = player.getAddress().getAddress();
            if (playerAddress != null && playerAddress.equals(address)) {
                connectionCount++;
            }

            if (connectionCount >= 2) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("Too many connections from this IP address.", NamedTextColor.RED));
                break;
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();
        if (message.startsWith("/") && !player.isOp()) {
            event.setCancelled(true);
            return;
        }
        if (player.hasPermission("venomgens.mod")) {
            if (message.contains("@everyone")) {
                String str = ChatColor.getLastColors(message);
                message = message.replace("@everyone", "§b@everyone" + (str.isEmpty() ? "§r" : str));
                for (Player p : Bukkit.getOnlinePlayers())
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                event.setMessage(message);
            }
        } else if (message.trim().toLowerCase().contains("nigger") || message
                .trim().toLowerCase().contains("nigga") || message.trim().toLowerCase().contains("faggot")) {
            event.setCancelled(true);
            player.sendMessage(String.format(event.getFormat(), player.getDisplayName(), message));
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (player.isOnline()) {
                    getServer().dispatchCommand(Bukkit.getConsoleSender(), "obliterate " + player.getName() + " Racism");
                } else {
                    getServer().dispatchCommand(Bukkit.getConsoleSender(), "banip " + player.getName() + " §c§lMay your wost nightmare come true, and may you suffer by the hands of your ruler, " + player.getName() + ". (Racism)");
                }
            }, 50L);
        }
    }
}
