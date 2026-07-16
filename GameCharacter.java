import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * A non-player character that occupies a room and may move independently.
 */
public class GameCharacter
{
    private final String name;
    private final String description;
    private final boolean mobile;
    private Room currentRoom;

    public GameCharacter(String name, String description, boolean mobile)
    {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("A character must have a name.");
        }
        if (description == null || description.trim().length() == 0) {
            throw new IllegalArgumentException("A character needs a description.");
        }
        this.name = name.trim().toLowerCase(Locale.ENGLISH);
        this.description = description.trim();
        this.mobile = mobile;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public Room getCurrentRoom()
    {
        return currentRoom;
    }

    public boolean isMobile()
    {
        return mobile;
    }

    /**
     * Places the character in exactly one room.
     */
    public void placeIn(Room room)
    {
        if (room == null) {
            throw new IllegalArgumentException("Character room cannot be null.");
        }
        if (currentRoom != null) {
            currentRoom.removeCharacter(this);
        }
        currentRoom = room;
        currentRoom.addCharacter(this);
    }

    /**
     * Moves a mobile character through a random ordinary exit.
     * Transporter rooms are excluded because their magic targets the player.
     */
    public boolean moveRandomly(Random random)
    {
        if (!mobile || currentRoom == null || random == null) {
            return false;
        }

        List<Room> possibleRooms = new ArrayList<Room>();
        for (Room room : currentRoom.getNeighbouringRooms()) {
            if (!room.isTransporter()) {
                possibleRooms.add(room);
            }
        }
        if (possibleRooms.isEmpty()) {
            return false;
        }

        placeIn(possibleRooms.get(random.nextInt(possibleRooms.size())));
        return true;
    }
}
