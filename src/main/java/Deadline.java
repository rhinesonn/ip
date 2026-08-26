/**
 * A task that must be completed by a specified date or time.
 */
public class Deadline extends Task {
    /** The date or time by which the task should be completed. */
    private final String by;

    /**
     * Creates a new incomplete deadline task.
     *
     * @param description the text describing the task
     * @param by the date or time by which the task should be completed
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the deadline text.
     *
     * @return the date or time attached to this deadline
     */
    public String getBy() {
        return by;
    }

    @Override
    protected TaskType getTaskType() {
        return TaskType.DEADLINE;
    }

    @Override
    protected String getTaskDetails() {
        return super.getTaskDetails() + " (by: " + by + ")";
    }

    @Override
    protected String getStorageDetails() {
        return super.getStorageDetails() + " | " + by;
    }
}
