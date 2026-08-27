package caitlyn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Tests event construction, accessors, display output, and persistence output. */
class EventTest {
    @Test
    void event_dateOnlyInputPreservesDateOnlyFormatting() {
        Event event = new Event("orientation week", "2025-03-14", "2025-03-16");

        assertEquals(LocalDateTime.of(2025, 3, 14, 0, 0), event.getFrom());
        assertEquals(LocalDateTime.of(2025, 3, 16, 0, 0), event.getTo());
        assertEquals("[E][ ] orientation week (from: Mar 14 2025 to: Mar 16 2025)",
                event.toString());
        assertEquals("E | 0 | orientation week | 2025-03-14 | 2025-03-16",
                event.toStorageString());
    }

    @Test
    void event_constructorsAcceptDateAndDateTimeValues() {
        Event fromDates = new Event("conference", LocalDate.of(2025, 3, 14), LocalDate.of(2025, 3, 16));
        Event fromDateTimes = new Event("meeting",
                LocalDateTime.of(2025, 3, 14, 9, 26), LocalDateTime.of(2025, 3, 14, 10, 0));

        assertEquals("[E][ ] conference (from: Mar 14 2025 to: Mar 16 2025)",
                fromDates.toString());
        assertEquals("[E][ ] meeting (from: Mar 14 2025 9:26 AM to: Mar 14 2025 10:00 AM)",
                fromDateTimes.toString());
        assertEquals("E | 0 | meeting | 2025-03-14T09:26 | 2025-03-14T10:00",
                fromDateTimes.toStorageString());
    }

    @Test
    void event_rejectsNullArgumentsAndInvalidDateText() {
        assertThrows(IllegalArgumentException.class,
                () -> new Event(null, "2025-03-14", "2025-03-16"));
        assertThrows(IllegalArgumentException.class,
                () -> new Event("event", (String) null, "2025-03-16"));
        assertThrows(IllegalArgumentException.class,
                () -> new Event("event", "2025-03-14", (String) null));
        assertThrows(IllegalArgumentException.class,
                () -> new Event("event", (LocalDate) null, LocalDate.of(2025, 3, 16)));
        assertThrows(IllegalArgumentException.class,
                () -> new Event("event", LocalDate.of(2025, 3, 14), (LocalDate) null));
        assertThrows(IllegalArgumentException.class,
                () -> new Event("event", (LocalDateTime) null, LocalDateTime.of(2025, 3, 16, 10, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> new Event("event", LocalDateTime.of(2025, 3, 14, 9, 0), (LocalDateTime) null));
        assertThrows(IllegalArgumentException.class,
                () -> new Event("event", "2025-02-30", "2025-03-16"));
    }
}
