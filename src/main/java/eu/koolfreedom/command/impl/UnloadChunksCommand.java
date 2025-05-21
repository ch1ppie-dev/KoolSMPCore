package eu.koolfreedom.command.impl;

import eu.koolfreedom.command.CommandParameters;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandParameters(name = "unloadchunks", description = "Unloads unused chunks from the server", aliases = {"rc", "uc"})
public class UnloadChunksCommand extends KoolCommand
{
    @Override
    public boolean run(CommandSender sender, Player player, Command cmd, String s, String[] args)
    {
        int numChunks = 0;

        for (World world : Bukkit.getServer().getWorlds())
        {
            numChunks += unloadUnusedChunks(world);
        }

        FUtil.staffAction(sender, "Unloaded unused chunks");
        msg(sender, "<gray>Unloaded " + numChunks + " chunks");
        return true;
    }

    private int unloadUnusedChunks(World world)
    {
        int numChunks = 0;

        for (Chunk chunk : world.getLoadedChunks())
        {
            chunk.unload();
            numChunks++;
        }

        return numChunks;
    }
}
