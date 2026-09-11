package caitlyn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Tests the storage/display markers assigned to every task type. */
class TaskTypeTest {
    @Test
    void getMarker_returnsTheExpectedMarkerForEveryTaskType() {
        assertEquals("T", TaskType.TODO.getMarker());
        assertEquals("D", TaskType.DEADLINE.getMarker());
        assertEquals("E", TaskType.EVENT.getMarker());
        assertEquals("W", TaskType.WITHIN.getMarker());
    }

    @Test
    void fromMarker_returnsTheMatchingTaskType() {
        assertEquals(TaskType.TODO, TaskType.fromMarker("T"));
        assertEquals(TaskType.DEADLINE, TaskType.fromMarker("D"));
        assertEquals(TaskType.EVENT, TaskType.fromMarker("E"));
        assertEquals(TaskType.WITHIN, TaskType.fromMarker("W"));
    }

    @Test
    void fromMarker_rejectsUnknownMarkers() {
        assertThrows(IllegalArgumentException.class, () -> TaskType.fromMarker("X"));
    }
}
