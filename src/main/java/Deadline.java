import java.util.ArrayList;
import java.util.List;

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
     * @throws IllegalArgumentException if a task field is null
     */
    public Deadline(String description, String by) {
        super(description);
        if (by == null) {
            throw new IllegalArgumentException("A deadline cannot be null.");
        }
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
    protected List<String> getStorageFields() {
        List<String> fields = new ArrayList<>(super.getStorageFields());
        fields.add(by);
        return fields;
    }
}
