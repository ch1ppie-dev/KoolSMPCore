package eu.koolfreedom.util;

import eu.koolfreedom.discord.StaffActionType;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

public class StaffActionLogger {
    private static final File logFile = new File("plugins/KoolSMPCore/staff-logs.yml");
    private static final YamlConfiguration config = YamlConfiguration.loadConfiguration(logFile);

    public static void log(StaffActionType type, String actor, String target, String reason) {
        String timestamp = Instant.now().toString();
        String id = UUID.randomUUID().toString(); // unique key per entry

        config.set(id + ".timestamp", timestamp);
        config.set(id + ".action", type.getLabel());
        config.set(id + ".actor", actor);
        config.set(id + ".target", target);
        config.set(id + ".reason", reason == null || reason.isBlank() ? "None specified" : reason);

        try {
            config.save(logFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
