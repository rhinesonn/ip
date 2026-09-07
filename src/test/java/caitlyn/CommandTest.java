package caitlyn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Tests validation shared by commands that select a task by number. */
class CommandTest {
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
}
