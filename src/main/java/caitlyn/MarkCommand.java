package caitlyn;

import java.util.List;

/**
 * A command that marks or unmarks one task.
 */
public final class MarkCommand extends Command {
    /** The complete mark or unmark command. */
    private final String command;

    /** Whether this command marks the selected task as done. */
    private final boolean markAsDone;

    /** Creates a mark or unmark command. */
    public MarkCommand(String command, boolean markAsDone) {
        this.command = command;
        this.markAsDone = markAsDone;
    }

    @Override
    public void execute(List<Task> tasks, Ui ui) throws CaitlynException {
        String commandName = markAsDone ? "mark" : "unmark";
        String[] commandParts = command.split("\\s+");
        if (commandParts.length != 2) {
            throw new CaitlynException("I beg your pardon, master. Please provide a task number, for example: "
                    + commandName + " 2.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(commandParts[1]);
        } catch (NumberFormatException exception) {
            throw new CaitlynException("I beg your pardon, master. Please provide a valid task number, for example: "
                    + commandName + " 2.");
        }

        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new CaitlynException("I beg your pardon, master, but I could not find task " + taskNumber + ".");
        }

        Task task = tasks.get(taskNumber - 1);
        boolean wasDone = task.isDone();
        if (markAsDone) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
        try {
            saveTasks(tasks);
        } catch (CaitlynException exception) {
            if (wasDone) {
                task.markAsDone();
            } else {
                task.markAsNotDone();
            }
            throw exception;
        }
        ui.showTaskStatus(task, markAsDone);
    }
}
