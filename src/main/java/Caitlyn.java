import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for the chatbot application.
 */
public class Caitlyn {
    /**
     * Starts the application, accepts tasks, displays the task list, and exits
     * when the user enters {@code bye}.
     *
     * @param args command-line arguments supplied when the program starts
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        List<Task> tasks = loadTasks(ui);
        boolean isExit = false;
        while (ui.hasNextCommand() && !isExit) {
            String fullCommand = ui.readCommand();

            ui.showSeparator();
            try {
                Command command = Parser.parse(fullCommand);
                command.execute(tasks, ui);
                isExit = command.isExit();
            } catch (CaitlynException exception) {
                ui.showError(exception.getMessage());
            } finally {
                ui.showSeparator();
            }
        }
    }

    /**
     * Loads the saved task list, falling back to an empty list when saved data cannot be read.
     *
     * @param ui the UI used to report loading errors
     * @return the saved tasks or an empty task list
     */
    private static List<Task> loadTasks(Ui ui) {
        try {
            return TaskStorage.load();
        } catch (IOException | IllegalArgumentException exception) {
            ui.showLoadingError();
            return new ArrayList<>();
        }
    }

}
