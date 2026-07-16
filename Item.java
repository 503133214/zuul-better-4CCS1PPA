import java.util.Locale;

/**
 * An object that can be placed in a room or carried by the player.
 */
public class Item
{
    private final String name;
    private final String description;
    private final int weight;
    private final boolean portable;

    public Item(String name, String description, int weight, boolean portable)
    {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("An item must have a name.");
        }
        if (description == null || description.trim().length() == 0) {
            throw new IllegalArgumentException("An item needs a description.");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Item weight must be positive.");
        }
        this.name = name.trim().toLowerCase(Locale.ENGLISH);
        this.description = description.trim();
        this.weight = weight;
        this.portable = portable;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public int getWeight()
    {
        return weight;
    }

    public boolean isPortable()
    {
        return portable;
    }

    public String getDisplayDescription()
    {
        String status = portable ? "portable" : "fixed in place";
        return name + " - " + description + " (" + weight + " kg, "
               + status + ")";
    }
}
