package eu.koolfreedom;

import eu.koolfreedom.api.Permissions;
import eu.koolfreedom.banning.Ban;
import eu.koolfreedom.banning.BanManager;
import eu.koolfreedom.banning.BanType;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.discord.Discord;
import eu.koolfreedom.log.FLog;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
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
public class KoolSMPCore extends JavaPlugin implements Listener
{
    @Getter
    private static KoolSMPCore instance;
    private static LuckPerms luckPermsAPI = null;
    public static final String CONFIG_FILENAME = "config.yml";
    public static final BuildProperties build = new BuildProperties();

    public BanManager bm;
    public MuteManager mum;
    public ServerListener sl;
    public Permissions perms;
    public ExploitListener el;
    public TabListener tl;
    public LoginListener lol; // lol

    public static LuckPerms getLuckPermsAPI()
    {
        if (luckPermsAPI == null)
        {
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null)
            {
                FLog.info("Successfully loaded the LuckPerms API.");
                luckPermsAPI = provider.getProvider();
            }
            else
            {
                FLog.error("The LuckPerms API was not loaded successfully. The plugin will not function properly.");
            }
        }

        return luckPermsAPI;
    }

    @Override
    public void onLoad()
    {
        instance = this;

        build.load(instance);
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
        loadBansConfig();
        FLog.info("Loaded configurations");

        if (getConfig().getBoolean("enable-announcer")) announcerRunnable();

        Discord.init();
        getLuckPermsAPI();
    }

    @Override
    public void onDisable()
    {
        FLog.info("KoolSMPCore has been disabled");

        if (Discord.getJDA() != null)
        {
            Discord.getJDA().shutdownNow();
        }

        //config.save();
        bm.save();
    }

    public void loadBansConfig()
    {
        bm = new BanManager();
        bm.load();
    }

    public void loadListeners()
    {
        mum = new MuteManager();
        sl = new ServerListener(this);
        el = new ExploitListener(this);
        lol = new LoginListener(this);
        tl = new TabListener(this);
    }

    public void loadCommands()
    {
        registerCommand("adminchat", new AdminChatCommand());
        registerCommand("ban", new BanCommand());
        registerCommand("banlist", new BanListCommand());
        registerCommand("clearchat", new ClearChatCommand());
        registerCommand("commandspy", new CommandSpyCommand());
        registerCommand("crash", new CrashCommand());
        registerCommand("cry", new CryCommand());
        registerCommand("doom", new DoomCommand());
        registerCommand("hug", new HugCommand());
        registerCommand("kick", new KickCommand());
        registerCommand("kiss", new KissCommand());
        registerCommand("koolsmpcore", new KoolSMPCoreCommand());
        registerCommand("mute", new MuteCommand());
        registerCommand("orbit", new OrbitCommand());
        registerCommand("pat", new PatCommand());
        registerCommand("poke", new PokeCommand());
        registerCommand("rawsay", new RawSayCommand());
        registerCommand("report", new ReportCommand());
        registerCommand("satisfyall", new SatisfyAllCommand());
        registerCommand("say", new SayCommand());
        registerCommand("ship", new ShipCommand());
        registerCommand("slap", new SlapCommand());
        registerCommand("smite", new SmiteCommand());
        registerCommand("spectate", new SpectateCommand());
        registerCommand("unban", new UnbanCommand());
        registerCommand("warn", new WarnCommand());
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

    private void registerCommand(String name, TabExecutor executor)
    {
        final PluginCommand cmd = Objects.requireNonNull(getCommand(name));

        cmd.setExecutor(executor);
        cmd.setTabCompleter(executor);
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

    // TODO: Rewrite this
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
                        DoomCommand.eviscerate(player, Bukkit.getConsoleSender(), "Hate Speech");
                    }
                    else
                    {
                        Ban ban = Ban.fromPlayer(player, Bukkit.getConsoleSender().getName(), "Hate Speech", BanType.BAN);
                        bm.addBan(ban);
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
