package eu.koolfreedom.freeze;

import eu.koolfreedom.KoolSMPCore;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FreezeManager
{
    private final Map<Player, FreezeData> frozenPlayers = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private boolean globalFreeze = false;

    public void freeze(Player player, long durationSeconds)
    {
        unfreeze(player);
        FreezeData data = new FreezeData(player);
        frozenPlayers.put(player, data);

        BukkitRunnable task = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                unfreeze(player);
            }
        };
        data.setUnfreezeTask(task.runTaskLater(KoolSMPCore.getInstance(), durationSeconds * 20));
    }

    public void unfreeze(Player player)
    {
        FreezeData data = frozenPlayers.remove(player);
        if (data != null) data.clearTask();
    }

    public boolean isFrozen(Player player)
    {
        return frozenPlayers.containsKey(player);
    }

    public FreezeData getData(Player player)
    {
        return frozenPlayers.get(player);
    }

    public void unfreezeAll()
    {
        frozenPlayers.keySet().forEach(this::unfreeze);
    }
}
