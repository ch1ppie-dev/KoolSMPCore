package eu.koolfreedom;

import eu.koolfreedom.bridge.GroupManagement;
import eu.koolfreedom.banning.BanManager;
import eu.koolfreedom.bridge.LuckPermsBridge;
import eu.koolfreedom.bridge.VanishIntegration;
import eu.koolfreedom.bridge.vanish.EssentialsVanishIntegration;
import eu.koolfreedom.command.CommandLoader;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.bridge.discord.DiscordSRVIntegration;
import eu.koolfreedom.bridge.DiscordIntegration;
import eu.koolfreedom.bridge.discord.EssentialsXDiscordIntegration;
import eu.koolfreedom.log.FLog;
import eu.koolfreedom.punishment.RecordKeeper;
import eu.koolfreedom.reporting.ReportManager;
import eu.koolfreedom.util.BuildProperties;
import eu.koolfreedom.util.FUtil;
import lombok.Getter;
import org.bukkit.plugin.java.*;
import org.bukkit.plugin.*;
import eu.koolfreedom.command.impl.*;
import eu.koolfreedom.listener.*;
import org.bukkit.*;
import java.util.*;

import org.bukkit.entity.*;

@Getter
public class KoolSMPCore extends JavaPlugin
{
    @Getter
    private static KoolSMPCore instance;

    private BuildProperties buildMeta;

    private CommandLoader commandLoader;

    private BanManager banManager;
    private MuteManager muteManager;
    private RecordKeeper recordKeeper;
    private ReportManager reportManager;

    private LoginListener loginListener;
    private ServerListener serverListener;
    private ExploitListener exploitListener;
    private ChatListener chatListener;

    private GroupManagement groupManager;
    private LuckPermsBridge luckPermsBridge;
    private DiscordIntegration<?> discordBridge;
    private VanishIntegration<?> vanishBridge;

    @Override
    public void onLoad()
    {
        instance = this;
        buildMeta = new BuildProperties();
    }

    @Override
    public void onEnable()
    {
        FLog.info("Created by gamingto12 and 0x7694C9");
        FLog.info("Version {}.{}", buildMeta.getVersion(), buildMeta.getNumber());
        FLog.info("Compiled {} by {}", buildMeta.getDate(), buildMeta.getAuthor());

        commandLoader = new CommandLoader(AdminChatCommand.class);
        commandLoader.loadCommands();
        FLog.info("Loaded commands");

        loadListeners();
        FLog.info("Loaded listeners");

        groupManager = new GroupManagement();
        FLog.info("Loaded group manager");

        loadBansConfig();
        FLog.info("Loaded configurations");

        if (ConfigEntry.ANNOUNCER_ENABLED.getBoolean())
        {
            announcerRunnable();
        }

        FLog.info("Bridges built");
        loadBridges();
    }

    @Override
    public void onDisable()
    {
        FLog.info("KoolSMPCore has been disabled");

        banManager.save();
        reportManager.save();
    }

    public void loadBansConfig()
    {
        banManager = new BanManager();
        banManager.load();
        recordKeeper = new RecordKeeper();
    }

    public void loadListeners()
    {
        muteManager = new MuteManager();
        reportManager = new ReportManager();
        serverListener = new ServerListener();
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) exploitListener = new ExploitListener();
        loginListener = new LoginListener();
        chatListener = new ChatListener();
    }

    public void loadBridges()
    {
        PluginManager pluginManager = Bukkit.getPluginManager();

        // Discord
        if (pluginManager.isPluginEnabled("DiscordSRV"))
        {
            FLog.info("Using DiscordSRV bridge.");
            discordBridge = new DiscordSRVIntegration().register();
        }
        else if (pluginManager.isPluginEnabled("EssentialsDiscord") && pluginManager.isPluginEnabled("EssentialsDiscordLink"))
        {
            FLog.info("Using EssentialsXDiscord bridge.");
            discordBridge = new EssentialsXDiscordIntegration().register();
        }
        else
        {
            discordBridge = Bukkit.getServicesManager().load(DiscordIntegration.class);

            if (discordBridge != null)
            {
                FLog.info("Using external Discord integrator {}", discordBridge.getClass().getName());
            }
        }

        // Vanish plugins
        if (pluginManager.isPluginEnabled("Essentials"))
        {
            vanishBridge = new EssentialsVanishIntegration();
        }
        else if (pluginManager.isPluginEnabled("SuperVanish"))
        {
            vanishBridge = new EssentialsVanishIntegration();
        }

        // LuckPerms
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms"))
        {
            luckPermsBridge = new LuckPermsBridge();
        }
    }

    public boolean isVanished(Player player)
    {
        return vanishBridge != null && vanishBridge.isVanished(player);
    }

    private void announcerRunnable()
    {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () ->
        {
            List<String> messages = ConfigEntry.ANNOUNCER_MESSAGES.getStringList();

            // Messages aren't configured, so we're not going to bother
            if (messages.isEmpty())
            {
                return;
            }

            FUtil.broadcast(false, messages.get(FUtil.randomNumber(0, messages.size())));
        }, 0L, ConfigEntry.ANNOUNCER_DELAY.getInteger());
    }
}
