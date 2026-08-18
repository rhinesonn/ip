/**
 * Represents one task in Caitlyn's task list.
 */
public class Task {
    /** The text that describes what needs to be done. */
    private final String description;

    /** Whether this task has been completed. */
    private boolean done;

    /**
     * Creates a new incomplete task.
     *
     * @param description the text describing the task
     */
    public Task(String description) {
        this.description = description;
        this.done = false;
    }

    /** Marks this task as completed. */
    public void markAsDone() {
        done = true;
    }

    /** Marks this task as incomplete again. */
    public void markAsNotDone() {
        done = false;
    }

    /**
     * Returns the task in the format used when displaying the task list.
     *
     * @return a status marker followed by the task description
     */
    @Override
    public String toString() {
        String status = done ? "X" : " ";
        return "[" + status + "] " + description;
    }
}
