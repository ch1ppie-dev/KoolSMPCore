package eu.koolfreedom;

import eu.koolfreedom.log.FLog;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.*;
import org.bukkit.plugin.*;
import com.comphenix.protocol.*;
import net.kyori.adventure.text.*;
import eu.koolfreedom.command.*;
import eu.koolfreedom.listener.ExploitListener;
import com.earth2me.essentials.*;
import org.bukkit.event.*;

import java.io.InputStream;
import java.util.concurrent.*;
import net.kyori.adventure.text.serializer.legacy.*;
import org.bukkit.*;
import org.bukkit.command.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.event.player.*;
import org.bukkit.entity.*;
@SuppressWarnings("deprecation")
public class KoolSMPCore extends JavaPlugin implements Listener {
    public static KoolSMPCore main;
    public static Server server;
    public static final Random random = new Random();
    public static String pluginVersion;
    public static String pluginName;
    public static final BuildProperties build = new BuildProperties();
    private final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public Component mmDeserialize(String message)
    {
        return MINI_MESSAGE.deserialize(message).clickEvent(null).hoverEvent(null);
    }


    public void onLoad()
    {
        main = this;
        server = main.getServer();
        KoolSMPCore.pluginName = main.getDescription().getName();
        KoolSMPCore.pluginVersion = main.getDescription().getVersion();

        FLog.setPluginLogger(main.getLogger());
        FLog.setServerLogger(getServer().getLogger());

        build.load(main);
    }

    @Override
    public void onEnable() {
        FLog.info("KoolSMPCore is starting...");
        FLog.info("KoolSMPCore has been enabled.");
        server.getPluginManager().registerEvents(this, this);

        Objects.requireNonNull(getCommand("clearchat")).setExecutor(new ClearChatCommand());
        Objects.requireNonNull(getCommand("crash")).setExecutor(new CrashCommand());
        Objects.requireNonNull(getCommand("doom")).setExecutor(new DoomCommand());
        Objects.requireNonNull(getCommand("hug")).setExecutor(new HugCommand());
        Objects.requireNonNull(getCommand("kiss")).setExecutor(new KissCommand());
        Objects.requireNonNull(getCommand("koolsmpcore")).setExecutor(new KoolSMPCoreCommand());
        Objects.requireNonNull(getCommand("lagsource")).setExecutor(new LagSourceCommand());
        Objects.requireNonNull(getCommand("obliterate")).setExecutor(new ObliterateCommand());
        Objects.requireNonNull(getCommand("pat")).setExecutor(new PatCommand());
        Objects.requireNonNull(getCommand("poke")).setExecutor(new PokeCommand());
        Objects.requireNonNull(getCommand("rawsay")).setExecutor(new RawSayCommand());
        Objects.requireNonNull(getCommand("report")).setExecutor(new ReportCommand());
        Objects.requireNonNull(getCommand("say")).setExecutor(new SayCommand());
        Objects.requireNonNull(getCommand("ship")).setExecutor(new ShipCommand());
        Objects.requireNonNull(getCommand("slap")).setExecutor(new SlapCommand());
        Objects.requireNonNull(getCommand("spectate")).setExecutor(new SpectateCommand());

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new ExploitListener(this));

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        if (getConfig().getBoolean("enable-announcer")) announcerRunnable();
    }

    @Override
    public void onDisable() {
        FLog.info("KoolSMPCore has been disabled");
    }

    public static class BuildProperties
    {
        public String author;
        public String codename;
        public String version;
        public String number;
        public String date;
        public String head;

        public void load(KoolSMPCore plugin)
        {
            try
            {
                final Properties props;

                try (InputStream in = plugin.getResource("build.properties"))
                {
                    props = new Properties();
                    props.load(in);
                }

                author = props.getProperty("buildAuthor", "gamingto12");
                codename = props.getProperty("buildCodeName", "unknown");
                version = props.getProperty("buildVersion", pluginVersion);
                number = props.getProperty("buildNumber", "1");
                date = props.getProperty("buildDate", "unknown");
                // Need to do this, or it will display ${git.commit.id.abbrev}
                head = props.getProperty("buildHead", "unknown").replace("${git.commit.id.abbrev}", "unknown");
            }
            catch (Exception ex)
            {
                FLog.severe("Could not load build properties! Did you compile with NetBeans/Maven?");
                FLog.severe(ex);
            }
        }

        public String formattedVersion()
        {
            return pluginVersion + "." + number + " (" + head + ")";
        }
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
                        getServer().dispatchCommand(Bukkit.getConsoleSender(), "obliterate " + player.getName() + " Slurs");
                    } else {
                        getServer().dispatchCommand(Bukkit.getConsoleSender(), "banip " + player.getName() + " §c§lMay your worst nightmare come true, and may you suffer by the hands of your ruler, " + player.getName() + ". (Slurs)");
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

    @SuppressWarnings("deprecation")
    public static void adminAction(String adminName, String action, boolean isRed)
    {
        KoolSMPCore.bcastMsg(adminName + " - " + action, (isRed ? ChatColor.RED : ChatColor.AQUA));
    }

    @SuppressWarnings("deprecation")
    public static void bcastMsg(String message, ChatColor color)
    {
        bcastMsg(message, color, true);
    }

    @SuppressWarnings("deprecation")
    public static void bcastMsg(String message, ChatColor color, Boolean toConsole)
    {
        if (toConsole)
        {
            Bukkit.getLogger().info(message);
        }

        for (Player player : Bukkit.getOnlinePlayers())
        {
            player.sendMessage((color == null ? "" : color) + message);
        }
    }

    public static void bcastMsg(String message, Boolean toConsole)
    {
        bcastMsg(message, null, toConsole);
    }

    public static void bcastMsg(String message)
    {
        KoolSMPCore.bcastMsg(message, null, true);
    }

    @SuppressWarnings("deprecation")
    public static String colorize(String string)
    {
        if (string != null)
        {
            Matcher matcher = Pattern.compile("&#[a-f0-9A-F]{6}").matcher(string);
            while (matcher.find())
            {
                String code = matcher.group().replace("&", "");
                string = string.replace("&" + code, net.md_5.bungee.api.ChatColor.of(code) + "");
            }

            string = ChatColor.translateAlternateColorCodes('&', string);
        }
        return string;
    }
}
