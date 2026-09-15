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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests persistence round trips and validation of the task file format. */
class TaskStorageTest {
    @TempDir
    private Path temporaryDirectory;

    private Path taskFile;
    private TaskStorage storage;

    @BeforeEach
    void setUpStorage() {
        taskFile = temporaryDirectory.resolve("data").resolve("duke.txt");
        storage = new TaskStorage(taskFile);
    }

    @Test
    void saveAndLoad_roundTripsTaskTypesStatusesAndEscapedFields() throws IOException {
        Todo todo = new Todo("review | notes\\backup");
        todo.markAsDone();
        Deadline deadline = new Deadline("return book", "2025-03-14 0926");
        Event event = new Event("project meeting", "2025-03-15", "2025-03-15 1600");
        List<Task> tasks = List.of(todo, deadline, event);

        storage.save(tasks);
        List<String> savedLines = Files.readAllLines(taskFile, StandardCharsets.UTF_8);
        List<Task> loadedTasks = storage.load();

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
        Files.deleteIfExists(taskFile);

        assertTrue(storage.load().isEmpty());
    }

    @Test
    void save_rejectsNullListsAndNullTasks() {
        assertThrows(IllegalArgumentException.class, () -> storage.save(null));
        List<Task> tasksWithNull = new ArrayList<>();
        tasksWithNull.add(new Todo("valid"));
        tasksWithNull.add(null);
        assertThrows(IllegalArgumentException.class, () -> storage.save(tasksWithNull));
    }

    @Test
    void load_rejectsMalformedDataAndReportsItsLineNumber() throws IOException {
        writeTaskFile(List.of(
                "T | 0 | valid task",
                "T | 2 | invalid status"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, storage::load);

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
    void saveAndLoad_controlCharacters_preservesDescription() throws IOException {
        String description = "first\nsecond\rthird | path\\backup";

        storage.save(List.of(new Todo(description)));

        assertEquals(description, storage.load().getFirst().getDescription());
    }

    @Test
    void load_blankLinesAndUnknownEscapes_preservesTaskData() throws IOException {
        writeTaskFile(List.of("", "   ", "T | 0 | path\\unknown"));

        List<Task> tasks = storage.load();

        assertEquals(1, tasks.size());
        assertEquals("path\\unknown", tasks.getFirst().getDescription());
    }

    @Test
    void save_invalidTaskList_preservesExistingFile() throws IOException {
        storage.save(List.of(new Todo("keep this")));
        List<Task> invalidTasks = new ArrayList<>();
        invalidTasks.add(new Todo("replacement"));
        invalidTasks.add(null);

        assertThrows(IllegalArgumentException.class, () -> storage.save(invalidTasks));

        assertEquals("keep this", storage.load().getFirst().getDescription());
    }

    @Test
    void save_replacementFails_removesTemporaryFile() throws IOException {
        Files.createDirectories(taskFile);
        Path existingFile = taskFile.resolve("keep.txt");
        Files.writeString(existingFile, "keep this");

        assertThrows(IOException.class, () -> storage.save(List.of(new Todo("replacement"))));

        assertEquals("keep this", Files.readString(existingFile));
        try (var savedPaths = Files.list(taskFile.getParent())) {
            assertEquals(List.of(taskFile), savedPaths.toList());
        }
    }

    @Test
    void save_separateStorageInstances_keepsFilesIndependent() throws IOException {
        TaskStorage otherStorage = new TaskStorage(temporaryDirectory.resolve("other.txt"));

        storage.save(List.of(new Todo("first file")));
        otherStorage.save(List.of(new Todo("second file")));

        assertEquals("first file", storage.load().getFirst().getDescription());
        assertEquals("second file", otherStorage.load().getFirst().getDescription());
    }

    /** Writes a temporary task file and verifies that loading reports the expected problem. */
    private void assertLoadFailsWith(String line, String expectedMessage) throws IOException {
        writeTaskFile(List.of(line));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, storage::load);

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    /** Writes task-file lines using the same UTF-8 encoding as the application. */
    private void writeTaskFile(List<String> lines) throws IOException {
        Files.createDirectories(taskFile.getParent());
        Files.write(taskFile, lines, StandardCharsets.UTF_8);
    }
}
