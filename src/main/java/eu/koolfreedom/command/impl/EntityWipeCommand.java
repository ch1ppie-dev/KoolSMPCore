package eu.koolfreedom.command.impl;

import eu.koolfreedom.command.CommandParameters;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

@CommandParameters(name = "entitywipe", description = "Wipe entities (not including players)", usage = "/<command> [mob,monster,mobs", aliases = {"ew", "mp"})
public class EntityWipeCommand extends KoolCommand
{
    public static final List<EntityType> NO_CLEAR = Arrays.asList(EntityType.ITEM_FRAME, EntityType.MINECART, EntityType.ARMOR_STAND);
    public EntityType et;

    public static int entities()
    {
        int removed = 0;
        for (World eworld : Bukkit.getWorlds())
        {
            removed = eworld.getEntities().stream().filter((entity) -> ( ! (entity instanceof Player) &&  ! NO_CLEAR.contains(entity.getType()))).map((entity) ->
            {
                entity.remove();
                return entity;
            }).map((_item) -> 1).reduce(removed, Integer :: sum);
        }
        return removed;
    }

    public static int mobs()
    {
        int removed = 0;
        for (World eworld : Bukkit.getWorlds())
        {
            removed = eworld.getLivingEntities().stream().filter((entity) -> (entity instanceof Creature || entity instanceof Ghast || entity instanceof Slime || entity instanceof EnderDragon || entity instanceof Ambient)).map((entity) ->
            {
                entity.remove();
                return entity;
            }).map((_item) -> 1).reduce(removed, Integer :: sum);
        }
        return removed;
    }

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String s, String[] args)
    {
        Player player = (Player) sender;

        if (args.length == 0)
        {
            FUtil.staffAction(sender, "Removed all entities");
            msg(sender, "<gray>Removed " + entities() + " entities.");
            return true;
        }
        if (args.length <= 1)
        {
            if (args[0].equalsIgnoreCase("mob") || args[0].equalsIgnoreCase("monsters") || args[0].equalsIgnoreCase("mobs"))
            {
                FUtil.staffAction(sender, "Purged all mobs");
                msg(sender, "<gray>Removed " + mobs() + " mobs");
                return true;
            }
            msg(sender, "<gray>Usage: /<command> [mobs,mob,monsters]");
            return true;
        }
        return true;
    }
}
