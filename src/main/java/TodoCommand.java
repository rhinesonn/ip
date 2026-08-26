import java.util.List;

/**
 * A command that creates a ToDo task.
 */
public final class TodoCommand extends Command {
    /** The description of the new task. */
    private final String description;

    /** Creates a ToDo command with its parsed description. */
    public TodoCommand(String description) {
        this.description = description;
    }

    @Override
    public void execute(List<Task> tasks, Ui ui) throws CaitlynException {
        if (description.isEmpty()) {
            throw new CaitlynException(
                    "I beg your pardon, master. I cannot prepare a task without a description.");
        }
        Task task = new Todo(description);
        addTask(tasks, task);
        ui.showTaskAdded(task, tasks.size());
    }
}
