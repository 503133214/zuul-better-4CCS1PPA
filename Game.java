import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Last Lantern - a text adventure based on World of Zuul.
 *
 * The player must feed a dwarf, receive a star wand, and relight the moon
 * beacon.  This class builds the world and coordinates commands; item,
 * inventory, room, parser, and character responsibilities live in their own
 * focused classes.
 */
public class Game
{
    private static final int MAXIMUM_CARRY_WEIGHT = 6;

    private final Parser parser;
    private final PrintStream output;
    private final Random random;
    private final List<Room> rooms;
    private final List<GameCharacter> characters;

    private Player player;
    private boolean dwarfHelped;
    private boolean won;

    /**
     * Creates a normal console game with non-deterministic challenge events.
     */
    public Game()
    {
        this(new InputStreamReader(System.in), System.out, new Random());
    }

    /**
     * Injectable constructor used by automated tests and scripted demos.
     */
    public Game(Reader input, PrintStream output, Random random)
    {
        this(new Parser(input, output), output, random);
    }

    /**
     * Creates a game from collaborating objects.
     */
    public Game(Parser parser, PrintStream output, Random random)
    {
        if (parser == null || output == null || random == null) {
            throw new IllegalArgumentException(
                "Parser, output, and random generator are required.");
        }
        this.parser = parser;
        this.output = output;
        this.random = random;
        this.rooms = new ArrayList<Room>();
        this.characters = new ArrayList<GameCharacter>();
        createWorld();
    }

    /**
     * Command-line entry point used by the executable JAR.
     */
    public static void main(String[] args)
    {
        new Game().play();
    }

    /**
     * Runs the game until the player wins or quits.
     */
    public void play()
    {
        printWelcome();

        boolean finished = false;
        while (!finished) {
            finished = processCommand(parser.getCommand());
        }

        if (won) {
            output.println("The Last Lantern burns again. Thank you for playing!");
        }
        else {
            output.println("Thank you for playing. Good bye.");
        }
    }

    /**
     * Exposes read-only state for tests and BlueJ inspection.
     */
    public Room getCurrentRoom()
    {
        return player.getCurrentRoom();
    }

    public boolean hasWon()
    {
        return won;
    }

    /**
     * Creates nine linked rooms, their items, and two characters.
     */
    private void createWorld()
    {
        Room gatehouse = new Room(
            "Ruined Gatehouse",
            "at the ruined gatehouse beneath a moonless sky");
        Room courtyard = new Room(
            "Echo Courtyard",
            "in the echoing courtyard of Lantern Keep");
        Room library = new Room(
            "Dust Library",
            "inside a library where silver dust covers every book");
        Room kitchen = new Room(
            "Old Kitchen",
            "in an old kitchen that still smells of warm bread");
        Room greatHall = new Room(
            "Great Hall",
            "in the great hall beneath a cracked glass moon");
        Room armory = new Room(
            "Silent Armory",
            "inside the silent armory of the vanished lantern guard");
        Room dwarfDen = new Room(
            "Dwarf Workshop",
            "in a cramped workshop filled with brass shavings");
        Room observatory = new Room(
            "Moon Observatory",
            "at the moon observatory beside the dark beacon");
        Room transporter = new Room(
            "Shimmer Chamber",
            "inside a chamber whose floor shimmers like water", true);

        gatehouse.setExit("north", courtyard);
        gatehouse.setExit("east", library);

        courtyard.setExit("south", gatehouse);
        courtyard.setExit("west", library);
        courtyard.setExit("east", kitchen);
        courtyard.setExit("north", greatHall);
        courtyard.setExit("down", transporter);

        library.setExit("west", gatehouse);
        library.setExit("east", courtyard);

        kitchen.setExit("west", courtyard);

        greatHall.setExit("south", courtyard);
        greatHall.setExit("west", armory);
        greatHall.setExit("east", dwarfDen);
        greatHall.setExit("north", observatory);

        armory.setExit("east", greatHall);
        dwarfDen.setExit("west", greatHall);
        observatory.setExit("south", greatHall);
        transporter.setExit("up", courtyard);

        gatehouse.addItem(new Item(
            "map", "a faded map showing the keep's main rooms", 1, true));
        courtyard.addItem(new Item(
            "coin", "a moon-stamped copper coin", 1, true));
        library.addItem(new Item(
            "lantern", "a small lantern with a cold blue flame", 2, true));
        library.addItem(new Item(
            "lectern", "a stone lectern bolted to the floor", 45, false));
        kitchen.addItem(new Item(
            "bread", "a fresh round loaf, surprisingly still warm", 1, true));
        kitchen.addItem(new Item(
            "cauldron", "an iron cauldron far too large to move", 70, false));
        greatHall.addItem(new Item(
            "statue", "a granite statue of the first lantern keeper", 200, false));
        armory.addItem(new Item(
            "rope", "a coil of strong silk rope", 3, true));
        armory.addItem(new Item(
            "shield", "a heavy shield engraved with a crescent", 5, true));
        armory.addItem(new Item(
            "anvil", "a massive blacksmith's anvil", 300, false));
        observatory.addItem(new Item(
            "beacon", "the fixed crystal beacon that must be relit", 500, false));

        GameCharacter dwarf = new GameCharacter(
            "dwarf", "a hungry engineer guarding a star wand", false);
        dwarf.placeIn(dwarfDen);
        characters.add(dwarf);

        GameCharacter raven = new GameCharacter(
            "raven", "a clockwork raven that patrols the keep", true);
        raven.placeIn(library);
        characters.add(raven);

        rooms.add(gatehouse);
        rooms.add(courtyard);
        rooms.add(library);
        rooms.add(kitchen);
        rooms.add(greatHall);
        rooms.add(armory);
        rooms.add(dwarfDen);
        rooms.add(observatory);
        rooms.add(transporter);

        player = new Player(gatehouse, MAXIMUM_CARRY_WEIGHT);
    }

    private void printWelcome()
    {
        output.println();
        output.println("THE LAST LANTERN");
        output.println("Lantern Keep has gone dark. Find a way to relight the");
        output.println("moon beacon before the last of its magic disappears.");
        output.println("Type 'help' for commands and 'hint' for your next goal.");
        output.println();
        output.println(player.getCurrentRoom().getLongDescription());
    }

    /**
     * Dispatches one parsed command.  True means the play loop should end.
     */
    private boolean processCommand(Command command)
    {
        if (command.hasExtraWords()) {
            output.println("Please use no more than three words.");
            return false;
        }
        if (command.isUnknown()) {
            output.println("I do not understand that command. Type 'help'.");
            return false;
        }

        String word = command.getCommandWord();
        if (word.equals("help")) {
            printHelp(command);
        }
        else if (word.equals("go")) {
            goRoom(command);
        }
        else if (word.equals("back")) {
            goBack(command);
        }
        else if (word.equals("look")) {
            look(command);
        }
        else if (word.equals("take")) {
            take(command);
        }
        else if (word.equals("drop")) {
            drop(command);
        }
        else if (word.equals("inventory")) {
            showInventory(command);
        }
        else if (word.equals("examine")) {
            examine(command);
        }
        else if (word.equals("talk")) {
            talk(command);
        }
        else if (word.equals("give")) {
            give(command);
        }
        else if (word.equals("use")) {
            return use(command);
        }
        else if (word.equals("hint")) {
            printHint(command);
        }
        else if (word.equals("wait")) {
            waitTurn(command);
        }
        else if (word.equals("quit")) {
            return quit(command);
        }
        return false;
    }

    private void printHelp(Command command)
    {
        if (hasArguments(command)) {
            output.println("The help command takes no object.");
            return;
        }
        output.println("Goal: find the star wand and use it on the moon beacon.");
        output.println("Commands (items and characters use one-word names):");
        parser.showCommands();
        output.println("Examples: go north | take bread | give bread dwarf");
        output.println("          use wand beacon | examine map | back");
        output.println("Carry limit: " + player.getMaximumCarryWeight() + " kg.");
    }

    private void goRoom(Command command)
    {
        if (!command.hasSecondWord()) {
            output.println("Go where?");
            return;
        }
        if (command.hasThirdWord()) {
            output.println("A direction must be one word.");
            return;
        }

        Room nextRoom = player.getCurrentRoom().getExit(command.getSecondWord());
        if (nextRoom == null) {
            output.println("There is no exit in that direction.");
            return;
        }

        Room previousRoom = player.getCurrentRoom();
        player.moveTo(nextRoom);
        enterCurrentRoom(previousRoom);
        moveCharacters();
        output.println(player.getCurrentRoom().getLongDescription());
    }

    private void goBack(Command command)
    {
        if (hasArguments(command)) {
            output.println("The back command takes no object.");
            return;
        }
        Room previousRoom = player.getCurrentRoom();
        if (!player.goBack()) {
            output.println("There is no previous room to return to.");
            return;
        }
        enterCurrentRoom(previousRoom);
        moveCharacters();
        output.println(player.getCurrentRoom().getLongDescription());
    }

    /**
     * Applies the transporter challenge whenever the player enters it.
     */
    private void enterCurrentRoom(Room previousRoom)
    {
        if (!player.getCurrentRoom().isTransporter()) {
            return;
        }

        List<Room> destinations = new ArrayList<Room>();
        for (Room room : rooms) {
            if (!room.isTransporter() && room != previousRoom) {
                destinations.add(room);
            }
        }
        Room destination = destinations.get(random.nextInt(destinations.size()));
        output.println("The chamber flashes. Space folds around you...");
        player.teleportTo(destination);
        output.println("You have been transported to " + destination.getName() + "!");
    }

    private void moveCharacters()
    {
        for (GameCharacter character : characters) {
            character.moveRandomly(random);
        }
    }

    private void look(Command command)
    {
        if (hasArguments(command)) {
            output.println("Use 'examine <item>' to inspect an object.");
            return;
        }
        output.println(player.getCurrentRoom().getLongDescription());
    }

    private void take(Command command)
    {
        if (!requireOneObject(command, "Take what?")) {
            return;
        }

        Item item = player.getCurrentRoom().getItem(command.getSecondWord());
        if (item == null) {
            output.println("There is no " + command.getSecondWord() + " here.");
            return;
        }
        if (!item.isPortable()) {
            output.println("The " + item.getName() + " is fixed in place.");
            return;
        }
        if (!player.addItem(item)) {
            output.println("That would exceed your "
                           + player.getMaximumCarryWeight() + " kg carry limit.");
            return;
        }

        player.getCurrentRoom().removeItem(item.getName());
        output.println("You take the " + item.getName() + ".");
        output.println("Carried weight: " + player.getCurrentWeight() + "/"
                       + player.getMaximumCarryWeight() + " kg.");
    }

    private void drop(Command command)
    {
        if (!requireOneObject(command, "Drop what?")) {
            return;
        }

        Item item = player.removeItem(command.getSecondWord());
        if (item == null) {
            output.println("You are not carrying " + command.getSecondWord() + ".");
            return;
        }
        player.getCurrentRoom().addItem(item);
        output.println("You drop the " + item.getName() + ".");
    }

    private void showInventory(Command command)
    {
        if (hasArguments(command)) {
            output.println("The inventory command takes no object.");
            return;
        }
        if (player.getInventory().isEmpty()) {
            output.println("You are carrying nothing (0/"
                           + player.getMaximumCarryWeight() + " kg).");
            return;
        }

        output.println("You are carrying:");
        for (Item item : player.getInventory()) {
            output.println("  " + item.getName() + " - " + item.getWeight() + " kg");
        }
        output.println("Total: " + player.getCurrentWeight() + "/"
                       + player.getMaximumCarryWeight() + " kg.");
    }

    private void examine(Command command)
    {
        if (!requireOneObject(command, "Examine what?")) {
            return;
        }

        String name = command.getSecondWord();
        Item item = player.getItem(name);
        if (item == null) {
            item = player.getCurrentRoom().getItem(name);
        }
        if (item != null) {
            output.println(item.getDisplayDescription());
            return;
        }

        GameCharacter character = player.getCurrentRoom().getCharacter(name);
        if (character != null) {
            output.println(character.getDescription());
            return;
        }
        output.println("There is no " + name + " here to examine.");
    }

    private void talk(Command command)
    {
        if (!requireOneObject(command, "Talk to whom?")) {
            return;
        }

        GameCharacter character =
            player.getCurrentRoom().getCharacter(command.getSecondWord());
        if (character == null) {
            output.println("That character is not here.");
        }
        else if (character.getName().equals("dwarf") && !dwarfHelped) {
            output.println("The dwarf says: 'Bring me bread and I will trade");
            output.println("you the star wand. Use: give bread dwarf.'");
        }
        else if (character.getName().equals("dwarf")) {
            output.println("The dwarf says: 'Take the wand north to the beacon.'");
        }
        else {
            output.println("The raven clicks: 'The hungry one guards starlight.'");
        }
    }

    /**
     * Three-word challenge command, for example "give bread dwarf".
     */
    private void give(Command command)
    {
        if (!command.hasSecondWord() || !command.hasThirdWord()) {
            output.println("Use: give <item> <character>.");
            return;
        }

        Item item = player.getItem(command.getSecondWord());
        if (item == null) {
            output.println("You are not carrying " + command.getSecondWord() + ".");
            return;
        }
        GameCharacter receiver =
            player.getCurrentRoom().getCharacter(command.getThirdWord());
        if (receiver == null) {
            output.println(command.getThirdWord() + " is not here.");
            return;
        }
        if (!receiver.getName().equals("dwarf") || !item.getName().equals("bread")) {
            output.println(receiver.getName() + " does not want the "
                           + item.getName() + ".");
            return;
        }
        if (dwarfHelped) {
            output.println("The dwarf has already made the trade.");
            return;
        }

        player.removeItem(item.getName());
        dwarfHelped = true;
        Item wand = new Item(
            "wand", "a silver star wand humming with moonlight", 2, true);
        output.println("The dwarf devours the bread and gives you the star wand.");
        if (player.addItem(wand)) {
            output.println("The wand is now in your inventory.");
        }
        else {
            player.getCurrentRoom().addItem(wand);
            output.println("Your pack is too heavy, so the dwarf places the wand here.");
        }
    }

    private boolean use(Command command)
    {
        if (!command.hasSecondWord()) {
            output.println("Use what?");
            return false;
        }

        Item item = player.getItem(command.getSecondWord());
        if (item == null) {
            output.println("You are not carrying " + command.getSecondWord() + ".");
            return false;
        }

        if (item.getName().equals("wand")) {
            if (!command.hasThirdWord()) {
                output.println("Use the wand on what?");
                return false;
            }
            if (command.getThirdWord().equals("beacon")
                    && player.getCurrentRoom().getName().equals("Moon Observatory")) {
                output.println("Silver fire leaps from the wand into the beacon.");
                output.println("Light pours across the keep and the sky answers.");
                output.println("*** YOU HAVE WON THE LAST LANTERN! ***");
                won = true;
                return true;
            }
            output.println("The wand sparks, but nothing answers here.");
            return false;
        }

        if (item.getName().equals("lantern")) {
            if (command.hasThirdWord()) {
                output.println("The blue lantern has no effect on the "
                               + command.getThirdWord() + ".");
            }
            else {
                output.println("The blue lantern reveals no hidden doorway.");
            }
        }
        else {
            String target = command.hasThirdWord()
                ? " on the " + command.getThirdWord() : "";
            output.println("Using the " + item.getName() + target
                           + " has no useful effect.");
        }
        return false;
    }

    private void printHint(Command command)
    {
        if (hasArguments(command)) {
            output.println("The hint command takes no object.");
            return;
        }
        if (!dwarfHelped && !player.hasItem("bread")) {
            Room breadRoom = findRoomContainingItem("bread");
            if (breadRoom != null) {
                output.println("Hint: the bread is in " + breadRoom.getName() + ".");
            }
            else {
                output.println("Hint: the dwarf still needs bread.");
            }
        }
        else if (!dwarfHelped) {
            output.println("Hint: the dwarf waits east of the great hall.");
        }
        else if (!player.hasItem("wand")) {
            Room wandRoom = findRoomContainingItem("wand");
            if (wandRoom != null) {
                output.println("Hint: the wand is in " + wandRoom.getName() + ".");
            }
            else {
                output.println("Hint: recover the star wand before going north.");
            }
        }
        else {
            output.println("Hint: go north from the great hall and use wand beacon.");
        }
    }

    private void waitTurn(Command command)
    {
        if (hasArguments(command)) {
            output.println("The wait command takes no object.");
            return;
        }
        moveCharacters();
        output.println("You wait. Somewhere nearby, metal wings scrape stone.");
        output.println(player.getCurrentRoom().getLongDescription());
    }

    private boolean quit(Command command)
    {
        if (hasArguments(command)) {
            output.println("Quit what?");
            return false;
        }
        return true;
    }

    private boolean requireOneObject(Command command, String missingMessage)
    {
        if (!command.hasSecondWord()) {
            output.println(missingMessage);
            return false;
        }
        if (command.hasThirdWord()) {
            output.println("That command accepts one object name.");
            return false;
        }
        return true;
    }

    private boolean hasArguments(Command command)
    {
        return command.hasSecondWord() || command.hasThirdWord();
    }

    private Room findRoomContainingItem(String itemName)
    {
        for (Room room : rooms) {
            if (room.getItem(itemName) != null) {
                return room;
            }
        }
        return null;
    }
}
