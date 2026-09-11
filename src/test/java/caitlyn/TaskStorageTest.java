package caitlyn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests persistence round trips and validation of the task file format. */
class TaskStorageTest {
    private static final Path TASK_FILE = Path.of("data", "duke.txt");

    private byte[] originalTaskFile;
    private boolean taskFileOriginallyExisted;

    @BeforeEach
    void preserveExistingTaskFile() throws IOException {
        taskFileOriginallyExisted = Files.exists(TASK_FILE);
        if (taskFileOriginallyExisted) {
            originalTaskFile = Files.readAllBytes(TASK_FILE);
        }
    }

    @AfterEach
    void restoreExistingTaskFile() throws IOException {
        Files.deleteIfExists(TASK_FILE);
        if (taskFileOriginallyExisted) {
            Files.createDirectories(TASK_FILE.getParent());
            Files.write(TASK_FILE, originalTaskFile);
        }
    }

    @Test
    void saveAndLoad_roundTripsTaskTypesStatusesAndEscapedFields() throws IOException {
        Todo todo = new Todo("review | notes\\backup");
        todo.markAsDone();
        Deadline deadline = new Deadline("return book", "2025-03-14 0926");
        Event event = new Event("project meeting", "2025-03-15", "2025-03-15 1600");
        List<Task> tasks = List.of(todo, deadline, event);

        TaskStorage.save(tasks);
        List<String> savedLines = Files.readAllLines(TASK_FILE, StandardCharsets.UTF_8);
        List<Task> loadedTasks = TaskStorage.load();

        assertEquals(List.of(
                "T | 1 | review \\| notes\\\\backup",
                "D | 0 | return book | 2025-03-14T09:26",
                "E | 0 | project meeting | 2025-03-15 | 2025-03-15T16:00"), savedLines);
        assertEquals(3, loadedTasks.size());
        assertInstanceOf(Todo.class, loadedTasks.get(0));
        assertInstanceOf(Deadline.class, loadedTasks.get(1));
        assertInstanceOf(Event.class, loadedTasks.get(2));
        assertEquals("review | notes\\backup", loadedTasks.get(0).getDescription());
        assertTrue(loadedTasks.get(0).isDone());
        assertEquals(LocalDateTime.of(2025, 3, 14, 9, 26), ((Deadline) loadedTasks.get(1)).getBy());
        assertEquals(LocalDateTime.of(2025, 3, 15, 0, 0), ((Event) loadedTasks.get(2)).getFrom());
        assertEquals(LocalDateTime.of(2025, 3, 15, 16, 0), ((Event) loadedTasks.get(2)).getTo());
    }

    @Test
    void load_returnsEmptyListWhenTaskFileDoesNotExist() throws IOException {
        Files.deleteIfExists(TASK_FILE);

        assertTrue(TaskStorage.load().isEmpty());
    }

    @Test
    void save_rejectsNullListsAndNullTasks() {
        assertThrows(IllegalArgumentException.class, () -> TaskStorage.save(null));
        List<Task> tasksWithNull = new ArrayList<>();
        tasksWithNull.add(new Todo("valid"));
        tasksWithNull.add(null);
        assertThrows(IllegalArgumentException.class, () -> TaskStorage.save(tasksWithNull));
    }

    @Test
    void load_rejectsMalformedDataAndReportsItsLineNumber() throws IOException {
        writeTaskFile(List.of(
                "T | 0 | valid task",
                "T | 2 | invalid status"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, TaskStorage::load);

        assertTrue(exception.getMessage().contains("line 2"));
        assertTrue(exception.getMessage().contains("status must be 0 or 1"));
    }

    @Test
    void load_rejectsUnknownTypesWrongFieldCountsAndIncompleteEscapes() throws IOException {
        assertLoadFailsWith("X | 0 | unknown", "Unknown saved task type");
        assertLoadFailsWith("T | 0", "type, status, and description");
        assertLoadFailsWith("T | 0 | too many | fields", "wrong number of fields");
        assertLoadFailsWith("T | 0 | incomplete\\", "incomplete escape sequence");
    }

    @Test
    void saveAndLoad_withinTasks_preservesPrecisionStatusesAndEscaping() throws IOException {
        List<Task> tasks = new ArrayList<>();
        for (String from : List.of("2027-01-15", "2027-01-15T09:00")) {
            for (String to : List.of("2027-01-25", "2027-01-25T17:00")) {
                WithinTask task = new WithinTask("collect | \\backup /from notes", from, to);
                tasks.add(task);
                WithinTask doneTask = new WithinTask("collect | \\backup /from notes", from, to);
                doneTask.markAsDone();
                tasks.add(doneTask);
            }
        }
        TaskStorage.save(tasks);
        List<Task> loadedTasks = TaskStorage.load();
        assertEquals(tasks.stream().map(Task::toStorageString).toList(),
                loadedTasks.stream().map(Task::toStorageString).toList());
        assertEquals(tasks.stream().map(Task::toString).toList(), loadedTasks.stream().map(Task::toString).toList());
        for (Task task : loadedTasks) {
            assertInstanceOf(WithinTask.class, task);
        }
    }

    @Test
    void load_invalidWithinRecords_rejectsEachInvalidField() throws IOException {
        assertLoadFailsWith("W | 0 | task | 2027-01-15", "wrong number of fields");
        assertLoadFailsWith("W | 0 | task | 2027-01-15 | 2027-01-25 | extra", "wrong number of fields");
        assertLoadFailsWith("W | 2 | task | 2027-01-15 | 2027-01-25", "status must be 0 or 1");
        assertLoadFailsWith("W | 0 |  | 2027-01-15 | 2027-01-25", "description cannot be empty");
        assertLoadFailsWith("W | 0 | task | 2027-02-30 | 2027-03-01", "valid start date");
        assertLoadFailsWith("W | 0 | task | 2027-01-15 | tomorrow", "valid end date");
        assertLoadFailsWith("W | 0 | task | 2027-01-15T09:00:01 | 2027-01-25", "start time must use minute precision");
        assertLoadFailsWith("W | 0 | task | 2027-01-15 | 2027-01-25T09:00:00.001",
                "end time must use minute precision");
        assertLoadFailsWith("W | 0 | task | 2027-01-26 | 2027-01-25", "must not be after its end");
    }

    @Test
    void load_mixedLegacyAndWithinRecords_retainsLegacyValidation() throws IOException {
        writeTaskFile(List.of("T | 1 | keep", "D | 0 | precise | 2027-01-15T09:00:01",
                "E | 0 | reversed | 2027-01-25 | 2027-01-15",
                "E | 1 | equal | 2027-01-15T09:00 | 2027-01-15T09:00",
                "W | 0 | new task | 15/1/2027 0900 | 25/1/2027"));
        List<Task> tasks = TaskStorage.load();
        assertEquals(5, tasks.size());
        TaskStorage.save(tasks);
        assertEquals(List.of("T | 1 | keep", "D | 0 | precise | 2027-01-15T09:00:01",
                "E | 0 | reversed | 2027-01-25 | 2027-01-15",
                "E | 1 | equal | 2027-01-15T09:00 | 2027-01-15T09:00",
                "W | 0 | new task | 2027-01-15T09:00 | 2027-01-25"), Files.readAllLines(TASK_FILE));
    }

    /** Writes a temporary task file and verifies that loading reports the expected problem. */
    private void assertLoadFailsWith(String line, String expectedMessage) throws IOException {
        writeTaskFile(List.of(line));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, TaskStorage::load);

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    /** Writes task-file lines using the same UTF-8 encoding as the application. */
    private void writeTaskFile(List<String> lines) throws IOException {
        Files.createDirectories(TASK_FILE.getParent());
        Files.write(TASK_FILE, lines, StandardCharsets.UTF_8);
    }
}
