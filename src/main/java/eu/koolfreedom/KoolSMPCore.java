package eu.koolfreedom;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketListener;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.CommandExecutor;
import com.earth2me.essentials.Essentials;
import com.sk89q.worldguard.WorldGuard;
import eu.koolfreedom.command.ClearChatCommand;
import eu.koolfreedom.command.ReportCommand;
import eu.koolfreedom.command.KoolSMPCoreCommand;
import eu.koolfreedom.command.SpectateCommand;
import eu.koolfreedom.command.ObliterateCommand;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.potion.PotionEffectType;
import eu.koolfreedom.system.IndefiniteBanSystem;

public class KoolSMPCore extends JavaPlugin implements Listener {
    public static KoolSMPCore main;
    private static RegionContainer container;
    public static final Random random = new Random();
    private final List<UUID> titleCooldown = new ArrayList<>();
    private int bossTaskId = 0;
    private static File indefbansFile;
    private static FileConfiguration indefbansConfig;
    private static final List<String> BAN_COMMANDS = new ArrayList<>(List.of("/ban", "/ban-ip", "/banip", "/ipban", "/tempban", "/tempbanip", "/tempipban", "/kick"));
    public void onEnable() {
        getLogger().info("KoolSMPCore has been enabled");

        IndefiniteBanSystem banSystem = new IndefiniteBanSystem(this);

        getServer().getPluginManager().registerEvents(this, (Plugin)this);

        getCommand("clearchat").setExecutor((CommandExecutor)new ClearChatCommand());
        getCommand("report").setExecutor((CommandExecutor)new ReportCommand());
        getCommand("spectate").setExecutor((CommandExecutor)new SpectateCommand());
        getCommand("obliterate").setExecutor((CommandExecutor)new ObliterateCommand());
        getCommand("koolsmpcore").setExecutor((CommandExecutor)new KoolSMPCoreCommand());
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener((PacketListener)new ExploitListener(this));

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        createIndefBansFile();

        if (getConfig().getBoolean("announcer-enabled"))
            announcerRunnable();
        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this, this::initBoss, 250L);
        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        main = this;
    }

    public void onDisable() {
        getLogger().info("KoolSMPCore has been disabled");

        saveIndefBansConfig();
    }

    private void initBoss() {
        World bossWorld = Bukkit.getWorld("boss");
        if (bossWorld != null) {
            for (Entity entity : bossWorld.getEntities()) {
                if (entity.getType() == EntityType.WARDEN)
                    entity.remove();
            }
            this.bossTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this, this::attemptSpawnBoss, 2400L);
        }
    }

    private void attemptSpawnBoss() {
        World bossWorld = Bukkit.getWorld("boss");
        if (bossWorld != null) {
            Location location = new Location(bossWorld, -1.0D, 2.0D, -14.0D);
            Warden entity = (Warden)bossWorld.spawnEntity(location, EntityType.WARDEN);
            entity.setRemoveWhenFarAway(false);
            Bukkit.broadcast(((TextComponent)Component.newline().append(Component.text("The warden has spawned in the boss arena.", (TextColor)NamedTextColor.RED)
                    .decoration(TextDecoration.BOLD, true))).appendNewline());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        return false;
    }

    private void createIndefBansFile() {
        indefbansFile = new File(getDataFolder(), "indefbans.yml");
        if (!indefbansFile.exists())
            try {
                getDataFolder().mkdirs();
                indefbansFile.createNewFile();
                saveResource("indefbans.yml", false);
            } catch (IOException e) {
                getLogger().severe("Error creating indefbans.yml: " + e.getMessage());
            }
        indefbansConfig = (FileConfiguration) YamlConfiguration.loadConfiguration(indefbansFile);
    }
    public static void saveIndefBansConfig() {
        try {
            indefbansConfig.save(indefbansFile);
          } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @EventHandler
    private void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        String[] commandParts = command.split(" ", 3);
        if (commandParts.length > 1) {
            boolean kick = commandParts[0].startsWith("/kick");
            if (BAN_COMMANDS.contains(commandParts[0]) || kick) {
                String playerName = commandParts[1];
                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null) {
                    String reason = "";
                    if (commandParts.length == 3) {
                        reason = commandParts[2];
                        Matcher matcher = TIME_PATTERN.matcher(reason);
                        while (matcher.find()) {
                            String timePart = matcher.group();
                            reason = reason.replace(timePart, "").trim();
                        }
                    }
                    if (reason.isEmpty() && !kick)
                        return;
                    Title title = Title.title(
                            (Component)Component.text("You were " + (kick ? "kicked" : "banned"), (TextColor)NamedTextColor.RED),
                            (Component)Component.text(reason, (TextColor)NamedTextColor.WHITE),
                            Title.Times.times(Duration.ofSeconds(1L), Duration.ofSeconds(5L), Duration.ofSeconds(1L)));
                    player.showTitle(title);
                }
            }
        }
    }
    public boolean isVanished(Player player) {
        Plugin essentials = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
        if (essentials == null)
            return false;
        return (essentials.isEnabled() && ((Essentials)essentials).getUser(player).isVanished());
    }
    public boolean isInvisible(Player player) {
        return (player.getGameMode() == GameMode.SPECTATOR || player.hasPotionEffect(PotionEffectType.INVISIBILITY) || isVanished(player));
    }
    private static final Pattern TIME_PATTERN = Pattern.compile("\\b(\\d+(?:\\.\\d+)?\\s*(?:s(?:ec(?:onds?)?)?|m(?:in(?:utes?)?)?|h(?:our(?:s?)?)?|d(?:ay(?:s?)?)?|w(?:eek(?:s?)?)?|mon(?:th(?:s?)?)?|y(?:ear(?:s?)?)?)\\b)");

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
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED)
            return;
        InetAddress address = event.getAddress();
        if (address == null)
            return;
        int found = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            InetAddress addr = player.getAddress().getAddress();
            found++;
            if (addr != null && addr.equals(address) && found >= 2) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, (Component)Component.text("Too many connections from this IP address.", (TextColor)NamedTextColor.RED));
                break;
            }
        }
    }
}