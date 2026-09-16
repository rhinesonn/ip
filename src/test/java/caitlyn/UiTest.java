package caitlyn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Tests the UI output adapter used by both Caitlyn interfaces. */
public class UiTest {
    @Test
    public void outputConstructor_showTaskAdded_sendsMessagesToSink() {
        List<String> messages = new ArrayList<>();
        Ui ui = new Ui(messages::add);

        ui.showTaskAdded(new Todo("read book"), 1);

        assertEquals(List.of(
                "     Got it. I've added this task:",
                "       [T][ ] read book",
                "     Now you have 1 tasks in the list."), messages);
    }

    @Test
    public void outputConstructor_hasNoInputSource() {
        Ui ui = new Ui(message -> { });

        assertFalse(ui.hasNextCommand());
    }

    @Test
    public void showLoadingError_failedLoad_explainsProtectionAndRecovery() {
        List<String> messages = new ArrayList<>();
        new Ui(messages::add).showLoadingError();
        assertEquals(List.of("     I could not read data/duke.txt. Task changes are disabled "
                + "to protect your saved data. Repair the file or its access permissions, "
                + "then restart Caitlyn."), messages);
    }

    @Test
    public void graphicalAdapter_welcome_omitsConsoleDecoration() {
        List<String> messages = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Ui ui = new Ui(messages::add, errors::add);

        ui.showWelcome();
        ui.showSeparator();

        assertEquals(List.of("Good day, master. I am Caitlyn, humbly at your service.",
                "How may I serve you today?"), messages);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void graphicalAdapter_errors_routesSeparatelyFromReplies() {
        List<String> messages = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Ui ui = new Ui(messages::add, errors::add);

        ui.showError("Invalid command");
        ui.showLoadingError();
        ui.showTasks(List.of(new Todo("keep this task")));

        assertEquals(2, errors.size());
        assertEquals("     Invalid command", errors.getFirst());
        assertTrue(errors.get(1).contains("Task changes are disabled"));
        assertEquals(List.of("     Here are the tasks in your list:",
                "     1.[T][ ] keep this task"), messages);
    }

    @Test
    public void consoleAdapter_errorsAndWelcome_retainsOriginalOutput() {
        List<String> messages = new ArrayList<>();
        Ui ui = new Ui(messages::add);

        ui.showWelcome();
        ui.showError("Invalid command");

        assertEquals(6, messages.size());
        assertTrue(messages.getFirst().startsWith("___"));
        assertTrue(messages.get(1).contains("____"));
        assertEquals(messages.getFirst(), messages.get(4));
        assertEquals("     Invalid command", messages.getLast());
    }

    @Test
    public void graphicalAdapter_missingDestination_rejectsConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new Ui(message -> { }, null));
        assertThrows(IllegalArgumentException.class, () -> new Ui(null, message -> { }));
    }
}
