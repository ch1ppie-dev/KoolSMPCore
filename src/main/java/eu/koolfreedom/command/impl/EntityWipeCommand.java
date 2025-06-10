package eu.koolfreedom.command.impl;

import eu.koolfreedom.command.CommandParameters;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.entity.Player;

import java.util.*;

@CommandParameters(name = "entitywipe", description = "Wipe entities (not including players)", usage = "/<command> [entity] [radius]", aliases = {"ew"})
public class EntityWipeCommand extends KoolCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String s, String[] args)
    {
        List<String> entityBlacklist = ConfigEntry.ENTITYWIPE_CLEAR_LIST.getStringList();

        List<String> entityWhitelist = new LinkedList<>(Arrays.asList(args));

        boolean radiusSpecified = !entityWhitelist.isEmpty() && isNumeric(entityWhitelist.get(entityWhitelist.size() - 1));
        boolean useBlacklist = args.length == 0 || (args.length == 1 && radiusSpecified);
        int radius = 0;

        if (radiusSpecified)
        {
            radius = parseInt(sender, args[entityWhitelist.size() - 1]); // get the args length as the size of the list
            radius *= radius;
            entityWhitelist.remove(entityWhitelist.size() - 1); // remove the radius from the list
        }

        EntityType[] entityTypes = EntityType.values();
        entityWhitelist.removeIf(name ->
        {
            boolean res = Arrays.stream(entityTypes).noneMatch(entityType -> name.equalsIgnoreCase(entityType.name()));
            if (res)
            {
                msg(sender, "<gray>Invalid mob type");
            }
            return res;
        });

        HashMap<String, Integer> entityCounts = new HashMap<>();

        for (World world : Bukkit.getWorlds())
        {
            for (Entity entity : world.getEntities())
            {
                if (entity.getType() != EntityType.PLAYER)
                {
                    String type = entity.getType().name();

                    if (useBlacklist ? entityBlacklist.stream().noneMatch(entityName -> entityName.equalsIgnoreCase(type)) : entityWhitelist.stream().anyMatch(entityName -> entityName.equalsIgnoreCase(type)))
                    {
                        if (radius > 0)
                        {
                            if (playerSender != null && entity.getWorld() == playerSender.getWorld() && playerSender.getLocation().distanceSquared(entity.getLocation()) > radius)
                            {
                                continue;
                            }
                        }
                    }
                }
            }
        }

        int entityCount = entityCounts.values().stream().mapToInt(a -> a).sum();

        if (useBlacklist)
        {
            FUtil.staffAction(sender, "Removed " + entityCount + " entities");
        }
        else
        {
            if (entityCount == 0)
            {
                msg(sender, "<gray>No entities were removed");
                return true;
            }
            String list = String.join(", ", entityCounts.keySet());
            list = list.replaceAll("(, )(?!.*\1)", (list.indexOf(", ") == list.lastIndexOf(", ") ? "" : ",") + " and ");
            FUtil.staffAction(sender, "Removed " + entityCount + " of type " + list);
        }
        return true;
    }

    private Integer parseInt(CommandSender sender, String string)
    {
        try
        {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            msg(sender, "<gray>Not a number");
        }
        return null;
    }

    private boolean isNumeric(String string)
    {
        if (string == null)
        {
            return false;
        }
        try
        {
            int num = Integer.parseInt(string);
        }
        catch (NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
}
