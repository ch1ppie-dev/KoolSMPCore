package eu.koolfreedom.util;

import eu.koolfreedom.KoolSMPCore;
import net.luckperms.api.LuckPerms;
import org.bukkit.Server;
import org.bukkit.Bukkit;

public class KoolSMPCoreBase
{
    protected static LuckPerms api = KoolSMPCore.getLuckPermsAPI();
    protected KoolSMPCore main = KoolSMPCore.main;
}
