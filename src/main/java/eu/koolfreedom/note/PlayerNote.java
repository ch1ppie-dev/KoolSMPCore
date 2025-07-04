package eu.koolfreedom.note;

import java.time.LocalDateTime;
import lombok.Getter;

public class PlayerNote
{
    @Getter
    private final String author;
    @Getter
    private final String message;
    @Getter
    private final LocalDateTime timestamp;

    public PlayerNote(String author, String message, LocalDateTime timestamp)
    {
        this.author = author;
        this.message = message;
        this.timestamp = timestamp;
    }
}
