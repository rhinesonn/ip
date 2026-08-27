package caitlyn;

/**
 * Converts complete user commands into executable command objects.
 */
public final class Parser {
    /** Prevents construction of this utility class. */
    private Parser() {
    }

    /**
     * Parses one complete command.
     *
     * @param fullCommand the command entered by the user.
     * @return the command object representing the input.
     */
    public static Command parse(String fullCommand) {
        String command = fullCommand.trim();
        if ("bye".equals(command)) {
            return new ExitCommand();
        } else if ("list".equals(command)) {
            return new ListCommand();
        } else if (command.equals("mark") || command.startsWith("mark ")) {
            return new MarkCommand(command, true);
        } else if (command.equals("unmark") || command.startsWith("unmark ")) {
            return new MarkCommand(command, false);
        } else if (command.equals("delete") || command.startsWith("delete ")) {
            return new DeleteCommand(command);
        } else if (command.equals("todo") || command.startsWith("todo ")) {
            return new TodoCommand(command.substring("todo".length()).trim());
        } else if (command.equals("deadline") || command.startsWith("deadline ")) {
            return new DeadlineCommand(command.substring("deadline".length()).trim());
        } else if (command.equals("event") || command.startsWith("event ")) {
            return new EventCommand(command.substring("event".length()).trim());
        }
        return new UnknownCommand();
    }
}
