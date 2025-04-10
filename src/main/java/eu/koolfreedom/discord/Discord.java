package eu.koolfreedom.discord;

import eu.koolfreedom.config.ConfigEntry;
import eu.koolfreedom.log.FLog;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Discord {

    private static JDA jda;

    public static void init() {
        if (jda != null) {
            FLog.info("[Discord] Bot is already initialized.");
            return;
        }

        String token = ConfigEntry.DISCORD_BOT_TOKEN.getString();
        if (token == null || token.isEmpty()) {
            FLog.warning("[Discord] Bot token missing from config.");
            return;
        }

        try {
            jda = JDABuilder.createLight(token)
                    .setEnabledIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .setDisabledIntents(GatewayIntent.GUILD_VOICE_STATES)
                    .build()
                    .awaitReady();
            FLog.info("[Discord] Discord bot initialized.");
        } catch (Exception e) {
            FLog.severe("[Discord] Failed to initialize Discord bot:");
            e.printStackTrace();
        }
    }

    public static JDA getJDA() {
        return jda;
    }
}
