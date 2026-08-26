import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A task that takes place between a start date/time and an end date/time.
 */
public class Event extends Task {
    /** The date and optional time at which the event starts. */
    private final LocalDateTime from;

    /** Whether the event start input explicitly included a time. */
    private final boolean fromHasTime;

    /** The date and optional time at which the event ends. */
    private final LocalDateTime to;

    /** Whether the event end input explicitly included a time. */
    private final boolean toHasTime;

    /**
     * Creates a new incomplete event task.
     *
     * @param description the text describing the event
     * @param from the event's start date or time
     * @param to the event's end date or time
     * @throws IllegalArgumentException if an event field is null or a date is invalid
     */
    public Event(String description, String from, String to) {
        this(description, DateTimeParser.parse(from), DateTimeParser.parse(to));
    }

    /** Creates an event from date-only start and end values. */
    public Event(String description, LocalDate from, LocalDate to) {
        this(description, dateOnly(from), dateOnly(to));
    }

    /** Creates an event from start and end date/time values. */
    public Event(String description, LocalDateTime from, LocalDateTime to) {
        this(description, new DateTimeParser.ParsedDateTime(from, true),
                new DateTimeParser.ParsedDateTime(to, true));
    }

    /** Creates an event from already parsed date/time values. */
    private Event(String description, DateTimeParser.ParsedDateTime from,
            DateTimeParser.ParsedDateTime to) {
        super(description);
        if (from == null || to == null) {
            throw new IllegalArgumentException("An event's start and end cannot be null.");
        }
        this.from = from.value();
        this.fromHasTime = from.hasTime();
        this.to = to.value();
        this.toHasTime = to.hasTime();
    }

    /**
     * Returns the event's start text.
     *
     * @return the event's start date and optional time
     */
    public LocalDateTime getFrom() {
        return from;
    }

    /**
     * Returns the event's end text.
     *
     * @return the event's end date and optional time
     */
    public LocalDateTime getTo() {
        return to;
    }

    @Override
    protected TaskType getTaskType() {
        return TaskType.EVENT;
    }

    @Override
    protected String getTaskDetails() {
        return super.getTaskDetails() + " (from: "
                + DateTimeParser.formatForDisplay(from, fromHasTime) + " to: "
                + DateTimeParser.formatForDisplay(to, toHasTime) + ")";
    }

    @Override
    protected List<String> getStorageFields() {
        List<String> fields = new ArrayList<>(super.getStorageFields());
        fields.add(DateTimeParser.formatForStorage(from, fromHasTime));
        fields.add(DateTimeParser.formatForStorage(to, toHasTime));
        return fields;
    }

    /** Converts a date-only constructor argument into the shared parsed representation. */
    private static DateTimeParser.ParsedDateTime dateOnly(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("An event date cannot be null.");
        }
        return new DateTimeParser.ParsedDateTime(date.atStartOfDay(), false);
    }
}
