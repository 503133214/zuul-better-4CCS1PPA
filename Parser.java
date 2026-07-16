import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Locale;

/**
 * Reads and validates console commands for the adventure game.
 */
public class Parser
{
    private final CommandWords commands;
    private final BufferedReader reader;
    private final PrintStream output;

    /**
     * Creates a parser connected to the console.
     */
    public Parser()
    {
        this(new InputStreamReader(System.in), System.out);
    }

    /**
     * Creates a parser using injectable input and output.  This constructor
     * keeps the parser testable without changing normal console behaviour.
     */
    public Parser(Reader input, PrintStream output)
    {
        if (input == null || output == null) {
            throw new IllegalArgumentException("Input and output are required.");
        }
        this.commands = new CommandWords();
        this.reader = new BufferedReader(input);
        this.output = output;
    }

    /**
     * Reads one line and returns a command containing at most three words.
     * End-of-file is treated as a clean quit so scripted runs cannot crash.
     */
    public Command getCommand()
    {
        output.print("> ");

        final String inputLine;
        try {
            inputLine = reader.readLine();
        }
        catch (IOException exception) {
            output.println("There was an error while reading: "
                           + exception.getMessage());
            return new Command(null, null, null, false);
        }

        if (inputLine == null) {
            return new Command("quit", null, null, false);
        }

        String trimmed = inputLine.trim().toLowerCase(Locale.ENGLISH);
        if (trimmed.length() == 0) {
            return new Command(null, null, null, false);
        }

        String[] words = trimmed.split("\\s+");
        String firstWord = words[0];
        String secondWord = words.length > 1 ? words[1] : null;
        String thirdWord = words.length > 2 ? words[2] : null;
        boolean extraWords = words.length > 3;

        if (!commands.isCommand(firstWord)) {
            firstWord = null;
        }
        return new Command(firstWord, secondWord, thirdWord, extraWords);
    }

    public void showCommands()
    {
        commands.showAll(output);
    }
}
