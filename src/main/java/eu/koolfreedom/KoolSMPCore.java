package eu.koolfreedom;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.earth2me.essentials.Essentials;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import eu.koolfreedom.command.*;
import net.kyori.adventure.title.Title;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.net.InetAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KoolSMPCore extends JavaPlugin implements Listener {
    public static KoolSMPCore main;
    private static RegionContainer container;
    public static final Random random = new Random();
    private final Map<UUID, PotionEffectWithDuration> trackedEffects = new HashMap<>();
    private final List<UUID> titleCooldown = new ArrayList<>();
    private static final List<String> BAN_COMMANDS = new ArrayList<>(List.of(
            "/ban",
            "/ban-ip",
            "/banip",
            "/ipban",
            "/tempban",
            "/tempbanip",
            "/tempipban",
            "/kick"
    ));

    /* private static final List<Material> CHRISTMAS_MATERIALS = new ArrayList<>(Arrays.asList(
            Material.DIAMOND_BLOCK,
            Material.ENCHANTED_GOLDEN_APPLE,
            Material.GOLDEN_APPLE,
            Material.NETHERITE_BLOCK,
            Material.NETHERITE_SWORD,
            Material.REDSTONE_BLOCK
    )); */

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
        // christmasRunnable();

        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
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

    private static final Pattern TIME_PATTERN = Pattern.compile("\\b(\\d+(?:\\.\\d+)?\\s*(?:s(?:ec(?:onds?)?)?|m(?:in(?:utes?)?)?|h(?:our(?:s?)?)?|d(?:ay(?:s?)?)?|w(?:eek(?:s?)?)?|mon(?:th(?:s?)?)?|y(?:ear(?:s?)?)?)\\b)");

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

                    if (reason.isEmpty() && !kick) {
                        return;
                    }

                    Title title = Title.title(
                            Component.text("You were " + (kick ? "kicked" : "banned"), NamedTextColor.RED),
                            Component.text(reason, NamedTextColor.WHITE),
                            Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(1))
                    );
                    player.showTitle(title);
                }
            }
        }
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

    /* private void christmasRunnable() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack christmas = new ItemStack(Material.DRIED_KELP_BLOCK);
                ItemMeta meta = christmas.getItemMeta();

                meta.displayName(Component.text("Christmas Gift", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false));
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                christmas.setItemMeta(meta);
                christmas.lore(List.of(Component.text("Right Click to redeem!", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false)));
                christmas.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);

                christmas.setAmount(random.nextInt(1, 3));

                if (!player.getInventory().addItem(christmas).isEmpty()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), christmas);
                }
                player.updateInventory();
            }
        }, 0L, 12000L);
    } */


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