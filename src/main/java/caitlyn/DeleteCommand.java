package caitlyn;

import java.util.List;

/**
 * A command that removes one task from the list.
 */
public final class DeleteCommand extends Command {
    /** The complete delete command. */
    private final String command;

    /**
     * Creates a delete command with its unparsed arguments.
     *
     * @param command the complete delete command entered by the user.
     */
    public DeleteCommand(String command) {
        this.command = command;
    }

    /**
     * Removes the selected task and saves the shortened task list.
     *
     * @param tasks the current task list.
     * @param ui the UI used to display the result.
     * @throws CaitlynException when the task number is invalid or saving fails.
     */
    @Override
    public void execute(List<Task> tasks, Ui ui) throws CaitlynException {
        String[] commandParts = command.split("\\s+");
        if (commandParts.length != 2) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please provide a task number, for example: "
                            + "delete 2.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(commandParts[1]);
        } catch (NumberFormatException exception) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please provide a valid task number, for example: "
                            + "delete 2.");
        }

        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new CaitlynException(
                    "I beg your pardon, master, but I could not find task " + taskNumber + ".");
        }

        Task removedTask = tasks.remove(taskNumber - 1);
        try {
            saveTasks(tasks);
        } catch (CaitlynException exception) {
            tasks.add(taskNumber - 1, removedTask);
            throw exception;
        }
        ui.showTaskDeleted(removedTask, tasks.size());
    }
}
