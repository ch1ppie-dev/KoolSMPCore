package eu.koolfreedom.command.impl;

import eu.koolfreedom.banning.BanManager;
import eu.koolfreedom.command.CommandParameters;
import eu.koolfreedom.command.KoolCommand;
import eu.koolfreedom.note.NoteManager;
import eu.koolfreedom.note.PlayerNote;
import eu.koolfreedom.listener.MuteManager;
import eu.koolfreedom.freeze.FreezeManager;
import eu.koolfreedom.util.FUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CommandParameters(name = "seen", description = "Shows information about a player or IP", usage = "/seen <player|ip>")
public class SeenCommand extends KoolCommand
{
    private final BanManager banManager = plugin.getBanManager();
    private final NoteManager noteManager = plugin.getNoteManager();
    private final MuteManager muteManager = plugin.getMuteManager();
    private final FreezeManager freezeManager = plugin.getFreezeManager();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    @Override
    public boolean run(CommandSender sender, Player player, Command cmd, String label, String[] args)
    {
        if (args.length != 1)
        {
            msg(sender, "<gray>Usage: /seen <player|ip>");
            return true;
        }

        String input = args[0];

        if (FUtil.isValidIP(input))
        {
            List<OfflinePlayer> playersWithIP = plugin.getAltManager().getAlts(input).stream()
                    .map(Bukkit::getOfflinePlayer)
                    .collect(Collectors.toList());

            if (playersWithIP.isEmpty())
            {
                msg(sender, "<red>No players found with IP <white>" + input + "</white>.");
                return true;
            }

            msg(sender, FUtil.miniMessage("<gold>Players with IP <white>" + input + "</white>:"));

            for (OfflinePlayer altPlayer : playersWithIP)
            {
                displayPlayerInfo(sender, altPlayer, input);
            }
            return true;
        }
        else
        {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayerIfCached(input);
            if (targetPlayer == null || (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()))
            {
                Player onlinePlayer = Bukkit.getPlayerExact(input);
                if (onlinePlayer != null)
                {
                    targetPlayer = onlinePlayer;
                }
                else
                {
                    msg(sender, "<red>Player not found: <white>" + input + "</white>");
                    return true;
                }
            }

            displayPlayerInfo(sender, targetPlayer, null);
        }

        return true;
    }

    private void displayPlayerInfo(CommandSender sender, OfflinePlayer target, String ipFilter)
    {
        UUID uuid = target.getUniqueId();
        String name = target.getName() != null ? target.getName() : "Unknown";

        // Header with username + UUID
        Component header = FUtil.miniMessage("<gold>Player Info: <white>" + name + "</white> <gray>(" + uuid + ")</gray>");
        msg(sender, header);

        // First join time
        Instant firstJoin = target.getFirstPlayed() > 0 ? Instant.ofEpochMilli(target.getFirstPlayed()) : null;
        String firstJoinStr = firstJoin != null ? formatter.format(firstJoin) : "Unknown";

        // Last seen
        Instant lastSeen = target.getLastPlayed() > 0 ? Instant.ofEpochMilli(target.getLastPlayed()) : null;
        String lastSeenStr = lastSeen != null ? formatter.format(lastSeen) : "Unknown";

        // Total playtime
        long playtimeSeconds = plugin.getPlaytimeManager().getPlaytime(uuid);
        String playtimeStr = formatDuration(playtimeSeconds);

        // IP address - only show if sender has permission
        String ipDisplayRaw = ipFilter != null ? ipFilter : plugin.getAltManager().getLastIP(uuid).orElse("Unknown");

        Component ipDisplay;
        if (sender.hasPermission("kfc.seen.viewip")) {
            ipDisplay = Component.text(ipDisplayRaw)
                    .clickEvent(ClickEvent.copyToClipboard(ipDisplayRaw))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy IP")))
                    .color(NamedTextColor.YELLOW);
        } else {
            ipDisplay = Component.text("<hidden>").color(NamedTextColor.GRAY);
        }

        // Alts by IP
        List<String> altNames = FUtil.getOfflinePlayersByIp(ipDisplayRaw).stream()
                .map(OfflinePlayer::getName)
                .filter(n -> !n.equalsIgnoreCase(name))
                .collect(Collectors.toList());

        // Notes and flags
        List<PlayerNote> notes = noteManager.getNotes(uuid);

        // Status - only check mute/freeze if player is online and a Player instance
        boolean isMuted = false;
        boolean isFrozen = false;
        if (target.isOnline() && target instanceof Player onlinePlayer) {
            isMuted = muteManager.isMuted(onlinePlayer);
            isFrozen = freezeManager.isFrozen(onlinePlayer);
        }

        boolean isBanned = banManager.isBanned(target);
        boolean isWhitelisted = target.isWhitelisted();
        boolean isOp = target.isOp();

        // Build the message string for MiniMessage (notes etc)
        StringBuilder sb = new StringBuilder();
        sb.append("<yellow>First Join:</yellow> <white>").append(firstJoinStr).append("\n");
        sb.append("<yellow>Last Seen:</yellow> <white>").append(lastSeenStr).append("\n");
        sb.append("<yellow>Playtime:</yellow> <white>").append(playtimeStr).append("\n");
        sb.append("<yellow>IP:</yellow> "); // We'll send ipDisplay component separately

        // Send header + details without IP first
        msg(sender, FUtil.miniMessage(sb.toString()));

        // Send an IP component separately (for clickable event)
        msg(sender, ipDisplay);

        if (!altNames.isEmpty()) {
            msg(sender, FUtil.miniMessage("<yellow>Alts:</yellow> <white>" + String.join(", ", altNames) + "</white>"));
        }

        if (!notes.isEmpty()) {
            msg(sender, FUtil.miniMessage("<yellow>Notes:</yellow>"));
            for (PlayerNote note : notes) {
                msg(sender, FUtil.miniMessage("  <gray>- [" + note.getTimestamp() + "] " + note.getAuthor() + ": " + note.getMessage() + "</gray>"));
            }
        } else {
            msg(sender, FUtil.miniMessage("<yellow>Notes:</yellow> <gray>None</gray>"));
        }

        String status = (isMuted ? "Muted" : "Not Muted") + ", " + (isFrozen ? "Frozen" : "Not Frozen");
        msg(sender, FUtil.miniMessage("<yellow>Status:</yellow> <white>" + status + "</white>"));
        msg(sender, FUtil.miniMessage("<yellow>OP:</yellow> <white>" + (isOp ? "Yes" : "No") + "</white>"));
        msg(sender, FUtil.miniMessage("<yellow>Banned:</yellow> <white>" + (isBanned ? "Yes" : "No") + "</white>"));
        msg(sender, FUtil.miniMessage("<yellow>Whitelisted:</yellow> <white>" + (isWhitelisted ? "Yes" : "No") + "</white>"));
    }

    private String formatDuration(long totalSeconds)
    {
        if (totalSeconds <= 0)
        {
            return "0s";
        }
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) result.append(days).append("d ");
        if (hours > 0) result.append(hours).append("h ");
        if (minutes > 0) result.append(minutes).append("m ");
        if (seconds > 0) result.append(seconds).append("s");

        return result.toString().trim();
    }

}
