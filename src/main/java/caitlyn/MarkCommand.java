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

    /**
     * Creates a mark or unmark command.
     *
     * @param command the complete command entered by the user.
     * @param markAsDone whether the command should mark the task as done.
     */
    public MarkCommand(String command, boolean markAsDone) {
        this.command = command;
        this.markAsDone = markAsDone;
    }

    /**
     * Changes the selected task's completion status and saves the result.
     *
     * @param tasks the current task list.
     * @param ui the UI used to display the result.
     * @throws CaitlynException when the task number is invalid or saving fails.
     */
    @Override
    public void execute(List<Task> tasks, Ui ui) throws CaitlynException {
        String commandName = markAsDone ? "mark" : "unmark";
        int taskIndex = parseTaskIndex(command, commandName, tasks);
        Task task = tasks.get(taskIndex);
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
