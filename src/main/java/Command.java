import java.io.IOException;
import java.util.List;

/**
 * Represents one command that Caitlyn can execute.
 */
public abstract class Command {
    /**
     * Executes this command against the current task list.
     *
     * @param tasks the current task list
     * @param ui the UI used to display results
     * @throws CaitlynException when the command cannot be carried out
     */
    public abstract void execute(List<Task> tasks, Ui ui) throws CaitlynException;

    /**
     * Returns whether this command ends the application.
     *
     * @return {@code true} only for the exit command
     */
    public boolean isExit() {
        return false;
    }

    /**
     * Adds a task and saves the updated list, rolling back if saving fails.
     *
     * @param tasks the current task list
     * @param task the task to add
     * @throws CaitlynException if the task cannot be saved
     */
    protected final void addTask(List<Task> tasks, Task task) throws CaitlynException {
        tasks.add(task);
        try {
            saveTasks(tasks);
        } catch (CaitlynException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
    }

    /**
     * Saves the current task list and turns a file-system error into a user-facing error.
     *
     * @param tasks the current task list
     * @throws CaitlynException if the task file cannot be written
     */
    protected final void saveTasks(List<Task> tasks) throws CaitlynException {
        try {
            TaskStorage.save(tasks);
        } catch (IOException | IllegalArgumentException | SecurityException exception) {
            throw new CaitlynException(
                    "I beg your pardon, master. I could not save your tasks to disk.");
        }
    }
}
