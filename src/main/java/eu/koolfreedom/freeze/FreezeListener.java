package eu.koolfreedom.freeze;

import eu.koolfreedom.KoolSMPCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class FreezeListener implements Listener {

    private final FreezeManager freezeManager = KoolSMPCore.getInstance().getFreezeManager();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (!event.getFrom().toVector().equals(event.getTo().toVector()))
        {
            if (freezeManager.isFrozen(event.getPlayer()) || freezeManager.isGlobalFreeze())
            {
                event.setTo(event.getFrom());
            }
        }
    }
}
