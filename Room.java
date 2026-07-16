import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * One location in the game world.
 *
 * A room owns its exits, items, and characters.  Linked maps keep display
 * order deterministic while allowing any number of entries.
 */
public class Room
{
    private final String name;
    private final String description;
    private final boolean transporter;
    private final Map<String, Room> exits;
    private final Map<String, Item> items;
    private final Map<String, GameCharacter> characters;

    /**
     * Retains the single-description constructor used by the starter code.
     */
    public Room(String description)
    {
        this(description, description, false);
    }

    public Room(String name, String description)
    {
        this(name, description, false);
    }

    public Room(String name, String description, boolean transporter)
    {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("A room must have a name.");
        }
        if (description == null || description.trim().length() == 0) {
            throw new IllegalArgumentException("A room needs a description.");
        }
        this.name = name.trim();
        this.description = description.trim();
        this.transporter = transporter;
        this.exits = new LinkedHashMap<String, Room>();
        this.items = new LinkedHashMap<String, Item>();
        this.characters = new LinkedHashMap<String, GameCharacter>();
    }

    public String getName()
    {
        return name;
    }

    public void setExit(String direction, Room neighbour)
    {
        if (direction == null || direction.trim().length() == 0
                || neighbour == null) {
            throw new IllegalArgumentException("An exit needs a direction and room.");
        }
        exits.put(normalise(direction), neighbour);
    }

    public Room getExit(String direction)
    {
        return direction == null ? null : exits.get(normalise(direction));
    }

    /**
     * Returns a snapshot so callers cannot alter this room's exits.
     */
    public List<Room> getNeighbouringRooms()
    {
        return Collections.unmodifiableList(
            new ArrayList<Room>(exits.values()));
    }

    public String getShortDescription()
    {
        return description;
    }

    public String getLongDescription()
    {
        StringBuilder text = new StringBuilder();
        text.append("You are ").append(description).append(".\n");
        text.append(getExitString()).append("\n");
        text.append(getItemString()).append("\n");
        text.append(getCharacterString());
        return text.toString();
    }

    public void addItem(Item item)
    {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add a null item.");
        }
        items.put(item.getName(), item);
    }

    public Item getItem(String itemName)
    {
        return itemName == null ? null : items.get(normalise(itemName));
    }

    public Item removeItem(String itemName)
    {
        return itemName == null ? null : items.remove(normalise(itemName));
    }

    public Collection<Item> getItems()
    {
        return Collections.unmodifiableCollection(items.values());
    }

    public GameCharacter getCharacter(String characterName)
    {
        return characterName == null
            ? null : characters.get(normalise(characterName));
    }

    public Collection<GameCharacter> getCharacters()
    {
        return Collections.unmodifiableCollection(characters.values());
    }

    void addCharacter(GameCharacter character)
    {
        characters.put(character.getName(), character);
    }

    void removeCharacter(GameCharacter character)
    {
        if (character != null) {
            characters.remove(character.getName());
        }
    }

    public boolean isTransporter()
    {
        return transporter;
    }

    private String getExitString()
    {
        StringBuilder text = new StringBuilder("Exits:");
        if (exits.isEmpty()) {
            return text.append(" none").toString();
        }
        for (String direction : exits.keySet()) {
            text.append(" ").append(direction);
        }
        return text.toString();
    }

    private String getItemString()
    {
        if (items.isEmpty()) {
            return "Items: none";
        }
        StringBuilder text = new StringBuilder("Items:");
        for (Item item : items.values()) {
            text.append(" ").append(item.getName())
                .append("[").append(item.getWeight()).append("kg");
            if (!item.isPortable()) {
                text.append(",fixed");
            }
            text.append("]");
        }
        return text.toString();
    }

    private String getCharacterString()
    {
        if (characters.isEmpty()) {
            return "Characters: none";
        }
        StringBuilder text = new StringBuilder("Characters:");
        for (GameCharacter character : characters.values()) {
            text.append(" ").append(character.getName());
        }
        return text.toString();
    }

    private static String normalise(String text)
    {
        return text.trim().toLowerCase(Locale.ENGLISH);
    }
}
