package eu.koolfreedom.note;

import java.util.*;

public class NoteManager
{
    private final Map<UUID, List<PlayerNote>> notes = new HashMap<>();

    public void addNote(UUID playerUUID, PlayerNote note)
    {
        notes.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(note);
    }

    public List<PlayerNote> getNotes(UUID playerUUID)
    {
        return notes.getOrDefault(playerUUID, List.of());
    }

    public boolean hasNotes(UUID playerUUID)
    {
        return notes.containsKey(playerUUID);
    }
}
