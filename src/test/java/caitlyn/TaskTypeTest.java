package caitlyn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests the storage/display markers assigned to every task type. */
class TaskTypeTest {
    @Test
    void getMarker_returnsTheExpectedMarkerForEveryTaskType() {
        assertEquals("T", TaskType.TODO.getMarker());
        assertEquals("D", TaskType.DEADLINE.getMarker());
        assertEquals("E", TaskType.EVENT.getMarker());
    }
}
