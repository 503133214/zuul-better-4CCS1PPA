import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Random;

/**
 * Dependency-free regression tests. Run with: java -ea GameTests
 */
public class GameTests
{
    private static int testsRun;

    public static void main(String[] args) throws Exception
    {
        testThreeWordParserAndExtraWords();
        testRoomCanHoldMultipleItems();
        testCarryLimitAndFixedItems();
        testMultiStepBackHistory();
        testMovingCharacterChangesRoom();
        testTransporterMovesPlayer();
        testEndToEndItemRules();
        testArgumentValidationAndDroppedItemHints();
        testWinningWalkthrough();
        System.out.println("All " + testsRun + " tests passed.");
    }

    private static void testThreeWordParserAndExtraWords() throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(bytes, true, "UTF-8");
        Parser parser = new Parser(
            new StringReader("  GIVE   bread   dwarf  \nuse wand beacon now\n"),
            output);

        Command give = parser.getCommand();
        check("give".equals(give.getCommandWord()), "first word is normalised");
        check("bread".equals(give.getSecondWord()), "second word is parsed");
        check("dwarf".equals(give.getThirdWord()), "third word is parsed");
        check(!give.hasExtraWords(), "valid three-word command is accepted");

        Command tooLong = parser.getCommand();
        check(tooLong.hasExtraWords(), "fourth word is reported");
        pass();
    }

    private static void testRoomCanHoldMultipleItems()
    {
        Room room = new Room("Test Room", "in a test room");
        room.addItem(new Item("apple", "a red apple", 1, true));
        room.addItem(new Item("table", "a fixed table", 20, false));

        check(room.getItems().size() == 2, "room holds multiple items");
        check(room.getItem("APPLE") != null, "item lookup ignores case");
        check(!room.getItem("table").isPortable(), "fixed item is represented");
        pass();
    }

    private static void testCarryLimitAndFixedItems()
    {
        Room room = new Room("Start", "at the start");
        Player player = new Player(room, 4);
        Item exact = new Item("crate", "a four kilogram crate", 4, true);
        Item extra = new Item("coin", "a one kilogram coin", 1, true);
        Item fixed = new Item("door", "a fixed iron door", 100, false);

        check(player.addItem(exact), "exact carry limit is permitted");
        check(player.getCurrentWeight() == 4, "carried weight is summed");
        check(!player.addItem(extra), "weight above limit is rejected");
        check(!player.addItem(fixed), "non-portable item is rejected");
        check(player.removeItem("crate") == exact, "drop removes carried item");
        check(player.getCurrentWeight() == 0, "drop releases carry weight");
        pass();
    }

    private static void testMultiStepBackHistory()
    {
        Room first = new Room("First", "in the first room");
        Room second = new Room("Second", "in the second room");
        Room third = new Room("Third", "in the third room");
        Player player = new Player(first, 5);

        player.moveTo(second);
        player.moveTo(third);
        check(player.goBack(), "first back succeeds");
        check(player.getCurrentRoom() == second, "first back reaches second room");
        check(player.goBack(), "second back succeeds");
        check(player.getCurrentRoom() == first, "second back reaches start");
        check(!player.goBack(), "back with empty history is safe");
        pass();
    }

    private static void testMovingCharacterChangesRoom()
    {
        Room first = new Room("First", "in the first room");
        Room second = new Room("Second", "in the second room");
        first.setExit("east", second);
        GameCharacter raven = new GameCharacter("raven", "a test raven", true);
        raven.placeIn(first);

        check(raven.moveRandomly(new FixedRandom(0)), "mobile character moves");
        check(raven.getCurrentRoom() == second, "character reaches neighbouring room");
        check(first.getCharacter("raven") == null, "old room releases character");
        check(second.getCharacter("raven") == raven, "new room owns character");
        pass();
    }

    private static void testTransporterMovesPlayer() throws Exception
    {
        GameRun run = runGame("go north\ngo down\nquit\n", new FixedRandom(0));
        check(run.text.contains("You have been transported to"),
              "transporter announces destination");
        check(!run.game.getCurrentRoom().isTransporter(),
              "player never remains in transporter room");
        pass();
    }

    private static void testEndToEndItemRules() throws Exception
    {
        String script =
            "go east\n" +
            "take lectern\n" +
            "take lantern\n" +
            "go east\n" +
            "go north\n" +
            "go west\n" +
            "take shield\n" +
            "take rope\n" +
            "inventory\n" +
            "quit\n";
        GameRun run = runGame(script, new FixedRandom(0));

        check(run.text.contains("lectern is fixed in place"),
              "fixed room item cannot be taken");
        check(run.text.contains("exceed your 6 kg carry limit"),
              "overweight item is rejected");
        check(run.text.contains("Total: 5/6 kg"),
              "inventory reports exact carried weight");
        pass();
    }

    private static void testWinningWalkthrough() throws Exception
    {
        String script =
            "go north\n" +
            "go east\n" +
            "take bread\n" +
            "go west\n" +
            "go north\n" +
            "go east\n" +
            "give bread dwarf\n" +
            "go west\n" +
            "go north\n" +
            "use wand beacon\n";
        GameRun run = runGame(script, new Random(7));

        check(run.game.hasWon(), "walkthrough sets win state");
        check(run.text.contains("YOU HAVE WON THE LAST LANTERN"),
              "win is clearly announced");
        check(!run.text.contains("I do not understand"),
              "walkthrough uses only valid commands");
        pass();
    }

    private static void testArgumentValidationAndDroppedItemHints()
        throws Exception
    {
        String script =
            "help object\n" +
            "go north\n" +
            "go east\n" +
            "take bread\n" +
            "go west\n" +
            "drop bread\n" +
            "hint\n" +
            "quit\n";
        GameRun run = runGame(script, new FixedRandom(0));

        check(run.text.contains("help command takes no object"),
              "help rejects unexpected arguments");
        check(run.text.contains("bread is in Echo Courtyard"),
              "hint follows a dropped quest item");
        pass();
    }

    private static GameRun runGame(String script, Random random) throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(bytes, true, "UTF-8");
        Game game = new Game(new StringReader(script), output, random);
        game.play();
        output.flush();
        return new GameRun(game, bytes.toString("UTF-8"));
    }

    private static void check(boolean condition, String message)
    {
        if (!condition) {
            throw new AssertionError("Failed: " + message);
        }
    }

    private static void pass()
    {
        testsRun++;
    }

    private static final class GameRun
    {
        private final Game game;
        private final String text;

        private GameRun(Game game, String text)
        {
            this.game = game;
            this.text = text;
        }
    }

    private static final class FixedRandom extends Random
    {
        private static final long serialVersionUID = 1L;
        private final int value;

        private FixedRandom(int value)
        {
            this.value = value;
        }

        @Override
        public int nextInt(int bound)
        {
            return Math.floorMod(value, bound);
        }
    }
}
