package caitlyn;

import java.util.List;

/**
 * A command that creates a deadline task.
 */
public final class DeadlineCommand extends Command {
    /** The text after the {@code deadline} command name. */
    private final String commandArguments;

    /**
     * Creates a deadline command with its unparsed arguments.
     *
     * @param commandArguments the text containing the task description and deadline.
     */
    public DeadlineCommand(String commandArguments) {
        this.commandArguments = commandArguments;
    }

    /**
     * Parses the deadline arguments, adds the new task, and saves it.
     *
     * @param tasks the current task list.
     * @param ui the UI used to display the result.
     * @throws CaitlynException when the command or deadline is invalid.
     */
    @Override
    public void execute(List<Task> tasks, Ui ui) throws CaitlynException {
        int markerIndex = commandArguments.indexOf("/by");
        if (markerIndex <= 0) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please provide a deadline in the format: "
                            + "deadline task /by date.");
        }

        String description = commandArguments.substring(0, markerIndex).trim();
        String by = commandArguments.substring(markerIndex + "/by".length()).trim();
        if (description.isEmpty() || by.isEmpty()) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please provide both a task description and a deadline.");
        }
        try {
            Task task = new Deadline(description, by);
            addTask(tasks, task);
            ui.showTaskAdded(task, tasks.size());
        } catch (IllegalArgumentException exception) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please use a valid date such as 2019-10-15 "
                            + "or 2/12/2019 1800.");
        }
    }
}
