package caitlyn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests command validation, persistence, and rollback when saving fails. */
class CommandTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    void execute_addCommands_persistsAllTaskTypes() throws IOException, CaitlynException {
        TaskStorage storage = new TaskStorage(temporaryDirectory.resolve("tasks.txt"));
        List<Task> tasks = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        Ui ui = new Ui(messages::add);

        new TodoCommand("read book", storage).execute(tasks, ui);
        new DeadlineCommand("return book /by 2025-03-14", storage).execute(tasks, ui);
        new EventCommand("meeting /from 2025-03-15 /to 2025-03-16", storage).execute(tasks, ui);

        assertEquals(List.of(
                "T | 0 | read book",
                "D | 0 | return book | 2025-03-14",
                "E | 0 | meeting | 2025-03-15 | 2025-03-16"),
                storage.load().stream().map(Task::toStorageString).toList());
        assertEquals(3, tasks.size());
        assertEquals("     Now you have 3 tasks in the list.", messages.getLast());
    }

    @Test
    void execute_markAndUnmark_persistsEachStatus() throws IOException, CaitlynException {
        TaskStorage storage = new TaskStorage(temporaryDirectory.resolve("tasks.txt"));
        Task task = new Todo("read book");
        List<Task> tasks = new ArrayList<>(List.of(task));
        List<String> messages = new ArrayList<>();
        Ui ui = new Ui(messages::add);

        new MarkCommand("mark 1", true, storage).execute(tasks, ui);

        assertTrue(task.isDone());
        assertTrue(storage.load().getFirst().isDone());
        assertEquals("       [T][X] read book", messages.getLast());

        new MarkCommand("unmark 1", false, storage).execute(tasks, ui);

        assertFalse(task.isDone());
        assertFalse(storage.load().getFirst().isDone());
        assertEquals("       [T][ ] read book", messages.getLast());
    }

    @Test
    void execute_deleteMiddleTask_preservesRemainingOrder() throws IOException, CaitlynException {
        TaskStorage storage = new TaskStorage(temporaryDirectory.resolve("tasks.txt"));
        Task firstTask = new Todo("first");
        Task lastTask = new Todo("last");
        List<Task> tasks = new ArrayList<>(List.of(firstTask, new Todo("middle"), lastTask));
        List<String> messages = new ArrayList<>();

        new DeleteCommand("delete 2", storage).execute(tasks, new Ui(messages::add));

        assertEquals(List.of(firstTask, lastTask), tasks);
        assertEquals(List.of("first", "last"), storage.load().stream().map(Task::getDescription).toList());
        assertEquals("     Now you have 2 tasks in the list.", messages.getLast());
    }

    @Test
    void execute_addSaveFailure_restoresOriginalList() throws IOException {
        TaskStorage storage = createFailingStorage();
        Task originalTask = new Todo("keep this");
        List<Task> tasks = new ArrayList<>(List.of(originalTask));
        List<Command> commands = List.of(
                new TodoCommand("read book", storage),
                new DeadlineCommand("return book /by 2025-03-14", storage),
                new EventCommand("meeting /from 2025-03-15 /to 2025-03-16", storage));

        for (Command command : commands) {
            assertSaveFailsWithoutConfirmation(command, tasks);
            assertEquals(List.of(originalTask), tasks);
        }
    }

    @Test
    void execute_statusSaveFailure_restoresPreviousStatus() throws IOException {
        TaskStorage storage = createFailingStorage();
        for (boolean wasDone : new boolean[] {false, true}) {
            for (boolean isMarkingDone : new boolean[] {false, true}) {
                Task task = new Todo("keep this");
                if (wasDone) {
                    task.markAsDone();
                }
                List<Task> tasks = new ArrayList<>(List.of(task));
                String commandText = isMarkingDone ? "mark 1" : "unmark 1";

                assertSaveFailsWithoutConfirmation(new MarkCommand(commandText, isMarkingDone, storage), tasks);

                assertEquals(wasDone, task.isDone());
                assertEquals(List.of(task), tasks);
            }
        }
    }

    @Test
    void execute_deleteSaveFailure_restoresTaskAtOriginalPosition() throws IOException {
        TaskStorage storage = createFailingStorage();
        Task removedTask = new Todo("middle");
        removedTask.markAsDone();
        List<Task> originalTasks = List.of(new Todo("first"), removedTask, new Todo("last"));
        List<Task> tasks = new ArrayList<>(originalTasks);

        assertSaveFailsWithoutConfirmation(new DeleteCommand("delete 2", storage), tasks);

        assertEquals(originalTasks, tasks);
        assertTrue(tasks.get(1).isDone());
    }

    @Test
    void taskSelection_rejectsMissingTaskNumber() {
        assertInvalidTaskSelection(new MarkCommand("mark", true),
                "I beg your pardon, master. Please provide a task number, for example: mark 2.");
        assertInvalidTaskSelection(new DeleteCommand("delete"),
                "I beg your pardon, master. Please provide a task number, for example: delete 2.");
    }

    @Test
    void taskSelection_rejectsNonNumericTaskNumber() {
        assertInvalidTaskSelection(new MarkCommand("mark one", true),
                "I beg your pardon, master. Please provide a valid task number, for example: mark 2.");
        assertInvalidTaskSelection(new DeleteCommand("delete one"),
                "I beg your pardon, master. Please provide a valid task number, for example: delete 2.");
    }

    @Test
    void taskSelection_rejectsTaskNumberOutsideList() {
        CaitlynException exception = assertThrows(CaitlynException.class, () ->
                new MarkCommand("mark 2", true)
                        .execute(List.of(new Todo("task")), new Ui(message -> { })));

        assertEquals("I beg your pardon, master, but I could not find task 2.",
                exception.getMessage());
    }

    /** Executes a command with an invalid task selection and checks its error message. */
    private void assertInvalidTaskSelection(Command command, String expectedMessage) {
        CaitlynException exception = assertThrows(CaitlynException.class, () ->
                command.execute(List.of(new Todo("task")), new Ui(message -> { })));

        assertEquals(expectedMessage, exception.getMessage());
    }

    /** Creates a deterministic save failure by placing a file where a directory is required. */
    private TaskStorage createFailingStorage() throws IOException {
        Path blockedDirectory = temporaryDirectory.resolve("blocked");
        Files.writeString(blockedDirectory, "existing file");
        return new TaskStorage(blockedDirectory.resolve("tasks.txt"));
    }

    /** Verifies that failed saves report the storage error without announcing success. */
    private void assertSaveFailsWithoutConfirmation(Command command, List<Task> tasks) {
        List<String> messages = new ArrayList<>();

        CaitlynException exception = assertThrows(CaitlynException.class, () ->
                command.execute(tasks, new Ui(messages::add)));

        assertEquals("I beg your pardon, master. I could not save your tasks to disk.", exception.getMessage());
        assertTrue(messages.isEmpty());
    }
}
