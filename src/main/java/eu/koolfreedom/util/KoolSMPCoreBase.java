package eu.koolfreedom.util;

import eu.koolfreedom.KoolSMPCore;
import net.luckperms.api.LuckPerms;

public class KoolSMPCoreBase
{
    protected static LuckPerms api = KoolSMPCore.getLuckPermsAPI();
    protected KoolSMPCore main = KoolSMPCore.getInstance();
}
