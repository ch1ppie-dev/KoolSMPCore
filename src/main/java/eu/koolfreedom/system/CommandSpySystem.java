package eu.koolfreedom.system;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import eu.koolfreedom.KoolSMPCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandSpySystem extends JavaPlugin implements Listener, CommandExecutor {
    private ArrayList <UUID> p;
    private FileConfiguration c;
    private File pF;
    private FileConfiguration pC;

    public CommandSpySystem(KoolSMPCore koolSMPCore) {
    }

    public void onEnable() {
        this.p = new ArrayList<>();
        lC();
        rE();
        rC();
        this.pC.getStringList("Players").forEach(x -> this.p.add(UUID.fromString(x)));
        u();
    }
    public void onDisable() {
        u();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!label.equalsIgnoreCase("commandspy") && !label.equalsIgnoreCase("cmdspy") && !label.equalsIgnoreCase("cspy"))
            return false;
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return false;
        }
        Player s = (Player)sender;
        if (!s.hasPermission("kf.admin")) {
            s.sendMessage(ChatColor.RED + "You do not have permission to execute this command");
            return false;
        }
        if (args.length != 0 && s.hasPermission("kf.cmdspy.others")) {
            Player t = Bukkit.getPlayer(args[0]);
            if (t == null) {
                s.sendMessage(ChatColor.GRAY + "Player could not be found.");
                return false;
            }
            if (this.p.contains(t.getUniqueId())) {
                this.p.remove(t.getUniqueId());
                u();
                t.sendMessage(ChatColor.translateAlternateColorCodes('&', this.c.getString("Disabled-Message")));
                s.sendMessage(ChatColor.translateAlternateColorCodes('&', this.c.getString("Disabled-Message-Others").replace("%player%", t.getName())));
                return true;
            }
            this.p.add(t.getUniqueId());
            u();
            t.sendMessage(ChatColor.translateAlternateColorCodes('&', this.c.getString("Enabled-Message")));
            s.sendMessage(ChatColor.translateAlternateColorCodes('&', this.c.getString("Enabled-Message-Others").replace("%player%", t.getName())));
            return true;
        }
        if (this.p.contains(s.getUniqueId())) {
            this.p.remove(s.getUniqueId());
            u();
            s.sendMessage(ChatColor.translateAlternateColorCodes('&', this.c.getString("Disabled-Message")));
            return true;
        }
        this.p.add(s.getUniqueId());
        u();
        s.sendMessage(ChatColor.translateAlternateColorCodes('&', this.c.getString("Enabled-Message")));
        return true;
    }
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (e.getPlayer().hasPermission("kf.cmdspy.exempt"))
            return;
        String m = e.getMessage().toLowerCase();
        List<String> i = this.c.getStringList("Ignored-Commands");
        if (i.contains(m.split(" ")[0]) || i.contains(m))
            return;
        String n = e.getPlayer().getName();
        String om = e.getMessage();
        getServer().getOnlinePlayers().forEach(x -> {
            if (this.p.contains(x.getUniqueId()) && !x.getUniqueId().equals(e.getPlayer().getUniqueId()))
                x.sendMessage(ChatColor.translateAlternateColorCodes('&', this.c.getString("Chat-Format").replace("%player%", n).replace("%message%", om)));
        });
    }

    private void u() {
        try {
            ArrayList<String> t = new ArrayList<>();
            this.p.forEach(x -> t.add(x.toString()));
            this.pC.set("Players", t);
            this.pC.save(this.pF);
        }   catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void rC() {
        getCommand("cmdspy").setExecutor(this);
        getCommand("commandspy").setExecutor(this);
        getCommand("cspy").setExecutor(this);
    }
    public void rE() {
        getServer().getPluginManager().registerEvents(this, (Plugin)this);
    }
    private void lC() {
        try {
            if (!getDataFolder().exists())
                getDataFolder().mkdirs();
            File cF = new File(getDataFolder(), "config.yml");
            if (!cF.exists())
                saveResource("Config.yml", false);
            this.c = (FileConfiguration)new YamlConfiguration();
            this.c.load(cF);
            this.pF = new File(getDataFolder(), "players.yml");
            if (!this.pF.exists())
                saveResource("players.yml", false);
            this.pC = (FileConfiguration)new YamlConfiguration();
            this.pC.load(this.pF);
        }   catch (Exception e) {
            e.printStackTrace();
        }
    }
}