import java.util.ArrayList;
import java.util.List;

/**
 * A task that takes place between a start date/time and an end date/time.
 */
public class Event extends Task {
    /** The date or time at which the event starts. */
    private final String from;

    /** The date or time at which the event ends. */
    private final String to;

    /**
     * Creates a new incomplete event task.
     *
     * @param description the text describing the event
     * @param from the event's start date or time
     * @param to the event's end date or time
     * @throws IllegalArgumentException if an event field is null
     */
    public Event(String description, String from, String to) {
        super(description);
        if (from == null || to == null) {
            throw new IllegalArgumentException("An event's start and end cannot be null.");
        }
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the event's start text.
     *
     * @return the event's start date or time
     */
    public String getFrom() {
        return from;
    }

    /**
     * Returns the event's end text.
     *
     * @return the event's end date or time
     */
    public String getTo() {
        return to;
    }

    @Override
    protected TaskType getTaskType() {
        return TaskType.EVENT;
    }

    @Override
    protected String getTaskDetails() {
        return super.getTaskDetails() + " (from: " + from + " to: " + to + ")";
    }

    @Override
    protected List<String> getStorageFields() {
        List<String> fields = new ArrayList<>(super.getStorageFields());
        fields.add(from);
        fields.add(to);
        return fields;
    }
}
