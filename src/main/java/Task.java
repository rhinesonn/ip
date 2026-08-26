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
     * Returns the kind of this task.
     *
     * @return the task type
     */
    protected TaskType getTaskType() {
        return TaskType.TODO;
    }

    /**
     * Returns the task description together with any type-specific details.
     *
     * @return the text displayed after the task status
     */
    protected String getTaskDetails() {
        return description;
    }

    /**
     * Returns this task in the format used by the task file.
     *
     * @return a pipe-separated representation of this task
     */
    public String toStorageString() {
        String doneMarker = done ? "1" : "0";
        return getTaskType().getMarker() + " | " + doneMarker + " | " + getStorageDetails();
    }

    /**
     * Returns the task fields that follow its type and completion marker.
     * Subclasses add their own date or time fields.
     *
     * @return the task description and any type-specific storage fields
     */
    protected String getStorageDetails() {
        return description;
    }

    /**
     * Returns the description of this task.
     *
     * @return the task description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns whether this task has been completed.
     *
     * @return {@code true} when the task is done
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Returns the task in the format used when displaying the task list.
     *
     * @return a status marker followed by the task description
     */
    @Override
    public String toString() {
        String status = done ? "X" : " ";
        return "[" + getTaskType().getMarker() + "][" + status + "] " + getTaskDetails();
    }
}
