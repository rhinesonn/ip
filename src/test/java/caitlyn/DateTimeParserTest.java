package caitlyn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Tests the supported date/time formats and conversions used by Caitlyn. */
class DateTimeParserTest {
    @Test
    void parse_acceptsIsoDateTime() {
        DateTimeParser.ParsedDateTime result = DateTimeParser.parse("2025-03-14T09:26");

        assertEquals(LocalDateTime.of(2025, 3, 14, 9, 26), result.value());
        assertTrue(result.hasTime());
    }

    @Test
    void parse_acceptsAllCommandDateTimeFormats() {
        assertEquals(LocalDateTime.of(2025, 3, 14, 9, 26),
                DateTimeParser.parse("2025-03-14 0926").value());
        assertEquals(LocalDateTime.of(2025, 3, 14, 9, 26),
                DateTimeParser.parse("14/3/2025 0926").value());
        assertEquals(LocalDateTime.of(2025, 3, 14, 9, 26),
                DateTimeParser.parse("2025-03-14 09:26").value());
        assertEquals(LocalDateTime.of(2025, 3, 14, 9, 26),
                DateTimeParser.parse("14/3/2025 09:26").value());
    }

    @Test
    void parse_acceptsDateOnlyFormatsAtMidnight() {
        DateTimeParser.ParsedDateTime isoResult = DateTimeParser.parse("2025-03-14");
        DateTimeParser.ParsedDateTime dayMonthYearResult = DateTimeParser.parse("14/3/2025");

        assertEquals(LocalDateTime.of(2025, 3, 14, 0, 0), isoResult.value());
        assertEquals(LocalDateTime.of(2025, 3, 14, 0, 0), dayMonthYearResult.value());
        assertFalse(isoResult.hasTime());
        assertFalse(dayMonthYearResult.hasTime());
    }

    @Test
    void parse_rejectsEmptyUnsupportedAndImpossibleDates() {
        assertThrows(IllegalArgumentException.class, () -> DateTimeParser.parse(null));
        assertThrows(IllegalArgumentException.class, () -> DateTimeParser.parse("   "));
        assertThrows(IllegalArgumentException.class, () -> DateTimeParser.parse("14-03-2025"));
        assertThrows(IllegalArgumentException.class, () -> DateTimeParser.parse("2025-02-30"));
        assertThrows(IllegalArgumentException.class, () -> DateTimeParser.parse("2025-03-14 2460"));
    }

    @Test
    void formatForDisplayAndStorage_preserveDateOnlyAndDateTimeValues() {
        LocalDateTime value = LocalDateTime.of(2025, 3, 14, 9, 26);
        DateTimeParser.ParsedDateTime parsedDateTime =
                new DateTimeParser.ParsedDateTime(value, true);

        assertEquals("Mar 14 2025", DateTimeParser.formatForDisplay(value, false));
        assertEquals("Mar 14 2025 9:26 AM", DateTimeParser.formatForDisplay(value, true));
        assertEquals("2025-03-14", DateTimeParser.formatForStorage(value, false));
        assertEquals("2025-03-14T09:26", DateTimeParser.formatForStorage(value, true));
        assertEquals("Mar 14 2025 9:26 AM", DateTimeParser.formatForDisplay(parsedDateTime));
        assertEquals("2025-03-14T09:26", DateTimeParser.formatForStorage(parsedDateTime));
    }

    @Test
    void parsedDateTime_rejectsNullValue() {
        assertThrows(IllegalArgumentException.class, () -> new DateTimeParser.ParsedDateTime(null, false));
    }
}
