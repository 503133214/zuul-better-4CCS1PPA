import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Holds player state: location, travel history, and carried items.
 */
public class Player
{
    private Room currentRoom;
    private final Deque<Room> roomHistory;
    private final Map<String, Item> inventory;
    private final int maximumCarryWeight;

    public Player(Room startingRoom, int maximumCarryWeight)
    {
        if (startingRoom == null) {
            throw new IllegalArgumentException("A player needs a starting room.");
        }
        if (maximumCarryWeight <= 0) {
            throw new IllegalArgumentException("Carry weight must be positive.");
        }
        this.currentRoom = startingRoom;
        this.maximumCarryWeight = maximumCarryWeight;
        this.roomHistory = new ArrayDeque<Room>();
        this.inventory = new LinkedHashMap<String, Item>();
    }

    public Room getCurrentRoom()
    {
        return currentRoom;
    }

    /**
     * Moves to a room and records the previous room for the back command.
     */
    public void moveTo(Room destination)
    {
        if (destination == null) {
            throw new IllegalArgumentException("Destination cannot be null.");
        }
        roomHistory.push(currentRoom);
        currentRoom = destination;
    }

    /**
     * Replaces a transient room with its teleport destination.  No additional
     * history entry is created, so back returns to the last meaningful room.
     */
    public void teleportTo(Room destination)
    {
        if (destination == null) {
            throw new IllegalArgumentException("Destination cannot be null.");
        }
        currentRoom = destination;
    }

    /**
     * Moves one step back through the full travel history.
     */
    public boolean goBack()
    {
        if (roomHistory.isEmpty()) {
            return false;
        }
        currentRoom = roomHistory.pop();
        return true;
    }

    public boolean canCarry(Item item)
    {
        return item != null && item.isPortable()
            && getCurrentWeight() + item.getWeight() <= maximumCarryWeight;
    }

    public boolean addItem(Item item)
    {
        if (!canCarry(item) || inventory.containsKey(item.getName())) {
            return false;
        }
        inventory.put(item.getName(), item);
        return true;
    }

    public Item removeItem(String itemName)
    {
        return itemName == null ? null
            : inventory.remove(normalise(itemName));
    }

    public Item getItem(String itemName)
    {
        return itemName == null ? null : inventory.get(normalise(itemName));
    }

    public boolean hasItem(String itemName)
    {
        return getItem(itemName) != null;
    }

    public Collection<Item> getInventory()
    {
        return Collections.unmodifiableCollection(inventory.values());
    }

    public int getCurrentWeight()
    {
        int total = 0;
        for (Item item : inventory.values()) {
            total += item.getWeight();
        }
        return total;
    }

    public int getMaximumCarryWeight()
    {
        return maximumCarryWeight;
    }

    private static String normalise(String text)
    {
        return text.trim().toLowerCase(Locale.ENGLISH);
    }
}
