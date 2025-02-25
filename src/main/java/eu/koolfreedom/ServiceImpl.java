package eu.koolfreedom;

import org.bukkit.event.Listener;

public abstract class ServiceImpl implements Listener
{
    protected final KoolSMPCore plugin;

    public ServiceImpl(KoolSMPCore plugin)
    {
        this.plugin = KoolSMPCore.getPlugin();
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    public abstract void onStart();

    public abstract void onStop();
}
