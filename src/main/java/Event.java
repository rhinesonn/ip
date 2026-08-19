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
     */
    public Event(String description, String from, String to) {
        super(description);
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
    protected String getTaskType() {
        return "E";
    }

    @Override
    protected String getTaskDetails() {
        return super.getTaskDetails() + " (from: " + from + " to: " + to + ")";
    }
}
