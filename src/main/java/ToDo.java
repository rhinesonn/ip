/**
 * A task that does not have a date or time attached to it.
 */
public class ToDo extends Task {
    /**
     * Creates a new incomplete ToDo task.
     *
     * @param description the text describing the task
     */
    public ToDo(String description) {
        super(description);
    }
}
