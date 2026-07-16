import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Stores the command vocabulary understood by the game.
 */
public class CommandWords
{
    private static final Set<String> VALID_COMMANDS =
        Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(
            "go", "back", "look", "take", "drop", "inventory",
            "examine", "talk", "give", "use", "hint", "wait",
            "help", "quit"
        )));

    /**
     * Returns true when the supplied word is a recognised command.
     */
    public boolean isCommand(String word)
    {
        return word != null && VALID_COMMANDS.contains(word);
    }

    /**
     * Prints all commands to the supplied output stream.
     */
    public void showAll(PrintStream output)
    {
        boolean first = true;
        for (String command : VALID_COMMANDS) {
            if (!first) {
                output.print("  ");
            }
            output.print(command);
            first = false;
        }
        output.println();
    }

    /**
     * Retained for compatibility with the original BlueJ project.
     */
    public void showAll()
    {
        showAll(System.out);
    }
}
