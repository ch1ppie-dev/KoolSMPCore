package eu.koolfreedom.discord;

import java.awt.Color;

public enum StaffActionType {
    BAN("Ban", Color.RED),
    MUTE("Mute", Color.ORANGE),
    KICK("Kick", Color.YELLOW),
    WARN("Warn", Color.MAGENTA),
    UNBAN("Unban", Color.GREEN),
    UNMUTE("Unmute", Color.CYAN);

    private final String label;
    private final Color color;

    StaffActionType(String label, Color color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }
}
