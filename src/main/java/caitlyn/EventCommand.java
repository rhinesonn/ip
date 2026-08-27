package caitlyn;

import java.util.List;

/**
 * A command that creates an event task.
 */
public final class EventCommand extends Command {
    /** The text after the {@code event} command name. */
    private final String command;

    /**
     * Creates an event command with its unparsed arguments.
     *
     * @param command the text containing the event description and date range.
     */
    public EventCommand(String command) {
        this.command = command;
    }

    /**
     * Parses the event arguments, adds the new task, and saves it.
     *
     * @param tasks the current task list.
     * @param ui the UI used to display the result.
     * @throws CaitlynException when the command or event dates are invalid.
     */
    @Override
    public void execute(List<Task> tasks, Ui ui) throws CaitlynException {
        int fromMarkerIndex = command.indexOf("/from");
        int toMarkerIndex = command.indexOf("/to");
        if (fromMarkerIndex <= 0 || toMarkerIndex <= fromMarkerIndex) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please provide an event in the format: "
                            + "event task /from start /to end.");
        }

        String description = command.substring(0, fromMarkerIndex).trim();
        String from = command.substring(fromMarkerIndex + "/from".length(), toMarkerIndex).trim();
        String to = command.substring(toMarkerIndex + "/to".length()).trim();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please provide a description, start time, "
                            + "and end time for the event.");
        }
        try {
            Task task = new Event(description, from, to);
            addTask(tasks, task);
            ui.showTaskAdded(task, tasks.size());
        } catch (IllegalArgumentException exception) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please use valid dates such as 2019-10-15 "
                            + "or 2/12/2019 1800.");
        }
    }
}
