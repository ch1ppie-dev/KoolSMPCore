package eu.koolfreedom.api;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import eu.koolfreedom.util.KoolSMPCoreBase;
import org.bukkit.plugin.Plugin;

public class EssentialsXBridge extends KoolSMPCoreBase
{
    private Essentials essentialsPlugin = null;

    public Essentials getEssentialsPlugin()
    {
        if (essentialsPlugin == null)
        {
            try
            {
                final Plugin essentials = server.getPluginManager().getPlugin("Essentials");
                if (essentials != null && essentials instanceof Essentials)
                {
                    essentialsPlugin = (Essentials)essentials;
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return essentialsPlugin;
    }

    public User getEssentialsUser(String username)
    {
        try
        {
            Essentials essentials = getEssentialsPlugin();
            if (essentials != null)
            {
                return essentials.getUserMap().getUser(username);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public void setNickname(String username, String nickname)
    {
        try
        {
            User user = getEssentialsUser(username);
            if (user != null)
            {
                user.setNickname(nickname);
                user.setDisplayNick();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
