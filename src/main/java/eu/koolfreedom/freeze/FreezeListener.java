package eu.koolfreedom.freeze;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;

public class FreezeListener implements Listener
{
    private final FreezeManager fm;

    public FreezeListener(FreezeManager fm)
    {
        this.fm = fm;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e)
    {
        Player p = e.getPlayer();

        if (!fm.isGlobalFreeze() && !fm.isFrozen(p))
        {
            return;
        }

        // keep them flying so that they don't fall endlessly
        p.setFlying(true);

        // cancel movement
        e.setTo(e.getFrom());
    }
}
