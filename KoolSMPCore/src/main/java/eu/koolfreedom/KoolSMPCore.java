package eu.koolfreedom;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import eu.koolfreedom.command.ClearChatCommand;
import eu.koolfreedom.command.ReportCommand;
import eu.koolfreedom.command.KoolSMPCoreCommand;
import eu.koolfreedom.command.SpectateCommand;
import eu.koolfreedom.command.ObliterateCommand;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.earth2me.essentials.Essentials;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class KoolSMPCore extends JavaPlugin implements Listener {
    public static KoolSMPCore main;
    private static File indefbansFile;
    private static FileConfiguration indefbansConfig;
    private static final List<String> BAN_COMMANDS = new ArrayList<>(List.of("/ban", "/ban-ip", "/banip", "/ipban", "/tempban", "/tempbanip", "/tempipban", "/kick"));
    public void onEnable() {
        getLogger().info("KoolSMPCore has been enabled");

        banSystem = new IndefiniteBanSystem(this);

        getServer().getPluginManager().registerEvents(this, (Plugin)this);

        getCommand("clearchat").setExecutor((CommandExecutor)new ClearChatCommand());
        getCommand("report").setExecutor((CommandExecutor)new ReportCommand());
        getCommand("spectate").setExecutor((CommandExecutor)new SpectateCommand());
        getCommand("obliterate").setExecutor((CommandExecutor)new ObliterateCommand());
        getcommand("koolsmpcore").setExecutor((CommandExecutor)new KoolSMPCoreCommand());

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        create indefbansConfig();

        if (getConfig().getBoolean("announcer-enabled")).announcerRunnable();
        container = WorldGuard.getInstance().getPlatform().getRegionContianer();
        main = this;
    }

    public void onDisable() {
        getLogger().info("KoolSMPCore has been disabled");

        save indefbansConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

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
        indefbansConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(indefbansFile);
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
        }0L,

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
    private static final class PotionEffectWithDuration extends Record {
        private final PotionEffect effect;

        private final int duration;

        private PotionEffectWithDuration(PotionEffect effect, int duration) {
            this.effect = effect;
            this.duration = duration;
        }
        public PotionEffect effect() {
            return this.effect;
        }

        public int duration() {
            return this.duration;
        }
    }
}