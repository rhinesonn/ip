package caitlyn;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A task that must be completed by a specified date or time.
 */
public class Deadline extends Task {
    /** The date and optional time by which the task should be completed. */
    private final LocalDateTime by;

    /** Whether the deadline input explicitly included a time. */
    private final boolean byHasTime;

    /**
     * Creates a new incomplete deadline task.
     *
     * @param description the text describing the task
     * @param by the date or time by which the task should be completed
     * @throws IllegalArgumentException if a task field is null or the date is invalid
     */
    public Deadline(String description, String by) {
        this(description, DateTimeParser.parse(by));
    }

    /**
     * Creates a deadline from a date without a time.
     *
     * @param description the task description
     * @param by the date by which the task should be completed
     */
    public Deadline(String description, LocalDate by) {
        this(description, dateOnly(by));
    }

    /**
     * Creates a deadline from a date and time.
     *
     * @param description the task description
     * @param by the date and time by which the task should be completed
     */
    public Deadline(String description, LocalDateTime by) {
        this(description, new DateTimeParser.ParsedDateTime(by, true));
    }

    /** Creates a deadline from an already parsed date/time value. */
    private Deadline(String description, DateTimeParser.ParsedDateTime by) {
        super(description);
        if (by == null) {
            throw new IllegalArgumentException("A deadline cannot be null.");
        }
        this.by = by.value();
        this.byHasTime = by.hasTime();
    }

    /**
     * Returns the deadline text.
     *
     * @return the date and optional time attached to this deadline
     */
    public LocalDateTime getBy() {
        return by;
    }

    @Override
    protected TaskType getTaskType() {
        return TaskType.DEADLINE;
    }

    @Override
    protected String getTaskDetails() {
        return super.getTaskDetails() + " (by: "
                + DateTimeParser.formatForDisplay(by, byHasTime) + ")";
    }

    @Override
    protected List<String> getStorageFields() {
        List<String> fields = new ArrayList<>(super.getStorageFields());
        fields.add(DateTimeParser.formatForStorage(by, byHasTime));
        return fields;
    }

    /** Converts a date-only constructor argument into the shared parsed representation. */
    private static DateTimeParser.ParsedDateTime dateOnly(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("A deadline cannot be null.");
        }
        return new DateTimeParser.ParsedDateTime(date.atStartOfDay(), false);
    }
}
