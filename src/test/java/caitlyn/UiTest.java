package caitlyn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
}
