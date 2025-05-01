package eu.koolfreedom;

import eu.koolfreedom.api.Permissions;
import eu.koolfreedom.banning.BanListener;
import eu.koolfreedom.banning.Bans;
import eu.koolfreedom.banning.PermBans;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.config.Configuration;
import eu.koolfreedom.config.MainConfig;
import eu.koolfreedom.discord.Discord;
import eu.koolfreedom.log.FLog;
import net.dv8tion.jda.api.JDA;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.luckperms.api.LuckPerms;
import org.bukkit.plugin.java.*;
import org.bukkit.plugin.*;
import net.kyori.adventure.text.*;
import eu.koolfreedom.command.impl.*;
import eu.koolfreedom.listener.*;
import com.earth2me.essentials.*;
import org.bukkit.event.*;
import java.io.InputStream;
import java.util.concurrent.*;
import net.kyori.adventure.text.serializer.legacy.*;
import org.bukkit.*;
import java.util.*;

import org.bukkit.event.player.*;
import org.bukkit.entity.*;

@SuppressWarnings("deprecation")
public class KoolSMPCore extends JavaPlugin implements Listener {
    public static KoolSMPCore main;
    public static final String CONFIG_FILENAME = "config.yml";
    public static final BuildProperties build = new BuildProperties();
    private final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    public Configuration config;
    public ServerListener sl;
    public Permissions perms;
    public ExploitListener el;
    public TabListener tl;
    public LoginListener lol; // lol
    public static JDA jda;

    public static KoolSMPCore getPlugin()
    {
        return main;
    }

    public Component mmDeserialize(String message)
    {
        return MINI_MESSAGE.deserialize(message).clickEvent(null).hoverEvent(null);
    }

    public Component mmDeserialize(String message, TagResolver... placeholders)
    {
        return MINI_MESSAGE.deserialize(message, placeholders);
    }

    public static LuckPerms getLuckPermsAPI()
    {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null)
        {
            FLog.info("Successfully loaded the LuckPerms API.");
            return provider.getProvider();
        }

        FLog.error("The LuckPerms API was not loaded successfully. The plugin will not function properly.");
        return null;
    }

    @Override
    public void onLoad()
    {
        main = this;
        config = new Configuration(this, "config.yml", true);

        build.load(main);
    }

    @Override
    public void onEnable()
    {
        FLog.info("Created by gamingto12 and 0x7694C9");
        FLog.info("Version " + build.version);
        FLog.info("Compiled " + build.date + " by " + build.author);
        Bukkit.getPluginManager().registerEvents(this, this);
        loadCommands();
        FLog.info("Loaded commands");
        loadListeners();
        FLog.info("Loaded listeners");
        perms = new Permissions();
        config.load();
        loadBansConfig();
        FLog.info("Loaded configurations");

        if (getConfig().getBoolean("enable-announcer")) announcerRunnable();

        Discord.init();
    }

    @Override
    public void onDisable()
    {
        FLog.info("KoolSMPCore has been disabled");
        if (jda != null) jda.shutdownNow();
        config.save();
    }

    public void loadBansConfig()
    {
        Bans bans = Bans.getConfig();
        bans.options().copyDefaults(true);
        bans.save();
        PermBans permbans = PermBans.getConfig();
        permbans.options().copyDefaults(true);
        permbans.save();
    }

    public void loadListeners()
    {
        PluginManager manager = Bukkit.getServer().getPluginManager();

        manager.registerEvents(new BanListener(this), this);
        manager.registerEvents(new MuteManager(this), this);
        sl = new ServerListener(this);
        el = new ExploitListener(this);
        lol = new LoginListener(this);
        tl = new TabListener(this);
    }

    public void loadCommands()
    {
        Objects.requireNonNull(getCommand("adminchat")).setExecutor(new AdminChatCommand());
        Objects.requireNonNull(getCommand("ban")).setExecutor(new BanCommand());
        Objects.requireNonNull(getCommand("clearchat")).setExecutor(new ClearChatCommand());
        Objects.requireNonNull(getCommand("commandspy")).setExecutor(new CommandSpyCommand());
        Objects.requireNonNull(getCommand("crash")).setExecutor(new CrashCommand());
        Objects.requireNonNull(getCommand("cry")).setExecutor(new CryCommand());
        Objects.requireNonNull(getCommand("doom")).setExecutor(new DoomCommand());
        Objects.requireNonNull(getCommand("hug")).setExecutor(new HugCommand());
        Objects.requireNonNull(getCommand("kick")).setExecutor(new KickCommand());
        Objects.requireNonNull(getCommand("kiss")).setExecutor(new KissCommand());
        Objects.requireNonNull(getCommand("koolsmpcore")).setExecutor(new KoolSMPCoreCommand());
        Objects.requireNonNull(getCommand("mute")).setExecutor(new MuteCommand());
        Objects.requireNonNull(getCommand("obliterate")).setExecutor(new ObliterateCommand());
        Objects.requireNonNull(getCommand("orbit")).setExecutor(new OrbitCommand());
        Objects.requireNonNull(getCommand("pat")).setExecutor(new PatCommand());
        Objects.requireNonNull(getCommand("permbans")).setExecutor(new PermBansCommand());
        Objects.requireNonNull(getCommand("poke")).setExecutor(new PokeCommand());
        Objects.requireNonNull(getCommand("rawsay")).setExecutor(new RawSayCommand());
        Objects.requireNonNull(getCommand("report")).setExecutor(new ReportCommand());
        Objects.requireNonNull(getCommand("satisfyall")).setExecutor(new SatisfyAllCommand());
        Objects.requireNonNull(getCommand("say")).setExecutor(new SayCommand());
        Objects.requireNonNull(getCommand("ship")).setExecutor(new ShipCommand());
        Objects.requireNonNull(getCommand("slap")).setExecutor(new SlapCommand());
        Objects.requireNonNull(getCommand("smite")).setExecutor(new SmiteCommand());
        Objects.requireNonNull(getCommand("spectate")).setExecutor(new SpectateCommand());
        Objects.requireNonNull(getCommand("unban")).setExecutor(new UnbanCommand());
        Objects.requireNonNull(getCommand("warn")).setExecutor(new WarnCommand());
    }

    public static class BuildProperties
    {
        public String author;
        public String version;
        public String number;
        public String date;

        void load(KoolSMPCore plugin)
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
                version = props.getProperty("buildVersion", plugin.getDescription().getVersion());
                number = props.getProperty("buildNumber", "1");
                date = props.getProperty("buildDate", "unknown");
            }
            catch (Exception ex)
            {
                FLog.error("Could not load build properties! Did you compile with NetBeans/Maven?", ex);
            }
        }
    }

    public boolean isVanished(Player player)
    {
        Plugin essentials = Bukkit.getServer().getPluginManager().getPlugin("Essentials");

        if (essentials == null) {
            return false;
        }

        return essentials.isEnabled() && ((Essentials) essentials).getUser(player).isVanished();
    }

    private void announcerRunnable()
    {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () ->
        {
            List<String> messageKeys = new ArrayList<>(getConfig().getConfigurationSection("messages").getKeys(false));
            if (messageKeys.isEmpty())
            {
                getLogger().warning("No messages found in configuration.");
                return;
            }
            String randomKey = messageKeys.get(ThreadLocalRandom.current().nextInt(messageKeys.size()));
            List<String> lines = getConfig().getStringList("messages." + randomKey);
            if (lines.isEmpty())
            {
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

        if (player.hasPermission("kf.admin"))
        {
            if (message.contains("@everyone"))
            {
                String lastColor = ChatColor.getLastColors(message);
                message = message.replace("@everyone", "§b@everyone" + (lastColor.isEmpty() ? "§r" : lastColor));
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                }

                event.setMessage(message);
                return;
            }
        }
        else
        {
            final String fuckYouJava = message;

            if (ConfigEntry.CHAT_FILTER_HATE_SPEECH.getStringList().stream()
                    .anyMatch(entry -> fuckYouJava.trim().toLowerCase().contains(entry)))
            {
                event.setCancelled(true);

                if (player.isOnline())
                {
                    player.sendMessage(String.format(event.getFormat(), player.getDisplayName(), message));
                }

                Bukkit.getScheduler().runTaskLater(this, () ->
                {
                    if (player.isOnline())
                    {
                        getServer().dispatchCommand(Bukkit.getConsoleSender(), "obliterate " + player.getName() + "Hate-Speech");
                    }
                    else
                    {
                        getServer().dispatchCommand(Bukkit.getConsoleSender(), "banip " + player.getName() + " Hate-Speech");
                    }
                }, 50L);

                return;
            }
        }

        for (Player p : Bukkit.getOnlinePlayers())
        {
            if (isVanished(p)) continue;

            if (message.contains("@" + p.getName()))
            {
                String lastColor = ChatColor.getLastColors(message);
                message = message.replace("@" + p.getName(), "§b@" + p.getName() + (lastColor.isEmpty() ? "§r" : lastColor));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
            }
        }
    }
}
