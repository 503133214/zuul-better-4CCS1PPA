/**
 * Represents one command entered by the player.
 *
 * A command can contain up to three words.  The third word supports commands
 * such as "give bread dwarf".  The parser also records whether extra words
 * were supplied so that the game can report the error instead of silently
 * ignoring input.
 */
public class Command
{
    private final String commandWord;
    private final String secondWord;
    private final String thirdWord;
    private final boolean extraWords;

    /**
     * Backwards-compatible constructor for one- and two-word commands.
     */
    public Command(String firstWord, String secondWord)
    {
        this(firstWord, secondWord, null, false);
    }

    /**
     * Creates a parsed command.
     */
    public Command(String firstWord, String secondWord, String thirdWord,
                   boolean extraWords)
    {
        this.commandWord = firstWord;
        this.secondWord = secondWord;
        this.thirdWord = thirdWord;
        this.extraWords = extraWords;
    }

    public String getCommandWord()
    {
        return commandWord;
    }

    public String getSecondWord()
    {
        return secondWord;
    }

    public String getThirdWord()
    {
        return thirdWord;
    }

    public boolean isUnknown()
    {
        return commandWord == null;
    }

    public boolean hasSecondWord()
    {
        return secondWord != null;
    }

    public boolean hasThirdWord()
    {
        return thirdWord != null;
    }

    public boolean hasExtraWords()
    {
        return extraWords;
    }
}
