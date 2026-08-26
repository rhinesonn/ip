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
        while (ui.hasNextCommand()) {
            String command = ui.readCommand();

            ui.showSeparator();
            try {
                if ("bye".equals(command)) {
                    ui.showFarewell();
                    break;
                }

                handleCommand(command, tasks, ui);
            } catch (CaitlynException exception) {
                ui.showError(exception.getMessage());
            }

            ui.showSeparator();
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

    /**
     * Interprets one user command and performs the requested action.
     *
     * @param command the trimmed command entered by the user
     * @param tasks the current task list
     * @param ui the UI used to display command results
     * @throws CaitlynException when the command cannot be carried out
     */
    private static void handleCommand(String command, List<Task> tasks, Ui ui) throws CaitlynException {
        if ("list".equals(command)) {
            ui.showTasks(tasks);
        } else if (command.equals("mark") || command.startsWith("mark ")) {
            changeTaskStatus(command, tasks, true, ui);
        } else if (command.equals("unmark") || command.startsWith("unmark ")) {
            changeTaskStatus(command, tasks, false, ui);
        } else if (command.equals("delete") || command.startsWith("delete ")) {
            deleteTask(command, tasks, ui);
        } else if (command.equals("todo") || command.startsWith("todo ")) {
            String description = command.substring("todo".length()).trim();
            if (description.isEmpty()) {
                throw new CaitlynException(
                        "I beg your pardon, master. I cannot prepare a task without a description.");
            }
            addTask(tasks, new Todo(description), ui);
        } else if (command.equals("deadline") || command.startsWith("deadline ")) {
            addDeadline(tasks, command.substring("deadline".length()).trim(), ui);
        } else if (command.equals("event") || command.startsWith("event ")) {
            addEvent(tasks, command.substring("event".length()).trim(), ui);
        } else {
            throw new CaitlynException(
                    "I humbly beg your pardon, master. I do not know how to carry out that command.");
        }
    }

    /**
     * Marks or unmarks a task after validating the task number.
     *
     * @param command the complete mark or unmark command
     * @param tasks the current task list
     * @param markAsDone whether the selected task should be marked as done
     * @param ui the UI used to display the result
     * @throws CaitlynException when the command has no valid task number
     */
    private static void changeTaskStatus(String command, List<Task> tasks, boolean markAsDone, Ui ui)
            throws CaitlynException {
        String commandName = markAsDone ? "mark" : "unmark";
        String[] commandParts = command.split("\\s+");
        if (commandParts.length != 2) {
            throw new CaitlynException("I beg your pardon, master. Please provide a task number, for example: "
                    + commandName + " 2.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(commandParts[1]);
        } catch (NumberFormatException exception) {
            throw new CaitlynException("I beg your pardon, master. Please provide a valid task number, for example: "
                    + commandName + " 2.");
        }

        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new CaitlynException("I beg your pardon, master, but I could not find task " + taskNumber + ".");
        }

        Task task = tasks.get(taskNumber - 1);
        boolean wasDone = task.isDone();
        if (markAsDone) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
        try {
            saveTasks(tasks);
        } catch (CaitlynException exception) {
            if (wasDone) {
                task.markAsDone();
            } else {
                task.markAsNotDone();
            }
            throw exception;
        }
        ui.showTaskStatus(task, markAsDone);
    }

    /**
     * Deletes a task after validating the task number and reports the updated task count.
     *
     * @param command the complete delete command
     * @param tasks the current task list
     * @param ui the UI used to display the result
     * @throws CaitlynException when the command has no valid task number
     */
    private static void deleteTask(String command, List<Task> tasks, Ui ui) throws CaitlynException {
        String[] commandParts = command.split("\\s+");
        if (commandParts.length != 2) {
            throw new CaitlynException("I beg your pardon, master. Please provide a task number, for example: "
                    + "delete 2.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(commandParts[1]);
        } catch (NumberFormatException exception) {
            throw new CaitlynException("I beg your pardon, master. Please provide a valid task number, for example: "
                    + "delete 2.");
        }

        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new CaitlynException("I beg your pardon, master, but I could not find task " + taskNumber + ".");
        }

        Task removedTask = tasks.remove(taskNumber - 1);
        try {
            saveTasks(tasks);
        } catch (CaitlynException exception) {
            tasks.add(taskNumber - 1, removedTask);
            throw exception;
        }
        ui.showTaskDeleted(removedTask, tasks.size());
    }

    /**
     * Adds a task and prints the standard confirmation message.
     *
     * @param tasks the current task list
     * @param task the task to add
     * @param ui the UI used to display the result
     */
    private static void addTask(List<Task> tasks, Task task, Ui ui) throws CaitlynException {
        tasks.add(task);
        try {
            saveTasks(tasks);
        } catch (CaitlynException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
        ui.showTaskAdded(task, tasks.size());
    }

    /**
     * Saves the current task list and turns a file-system error into a user-facing error.
     *
     * @param tasks the current task list
     * @throws CaitlynException if the task file cannot be written
     */
    private static void saveTasks(List<Task> tasks) throws CaitlynException {
        try {
            TaskStorage.save(tasks);
        } catch (IOException | IllegalArgumentException | SecurityException exception) {
            throw new CaitlynException(
                    "I beg your pardon, master. I could not save your tasks to disk.");
        }
    }

    /**
     * Parses and adds a deadline command.
     *
     * @param tasks the current task list
     * @param command the part of the command after {@code deadline}
     * @param ui the UI used to display the result
     */
    private static void addDeadline(List<Task> tasks, String command, Ui ui) throws CaitlynException {
        int markerIndex = command.indexOf("/by");
        if (markerIndex <= 0) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please provide a deadline in the format: deadline task /by date.");
        }

        String description = command.substring(0, markerIndex).trim();
        String by = command.substring(markerIndex + "/by".length()).trim();
        if (description.isEmpty() || by.isEmpty()) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please provide both a task description and a deadline.");
        }
        try {
            addTask(tasks, new Deadline(description, by), ui);
        } catch (IllegalArgumentException exception) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please use a valid date such as 2019-10-15 or 2/12/2019 1800.");
        }
    }

    /**
     * Parses and adds an event command.
     *
     * @param tasks the current task list
     * @param command the part of the command after {@code event}
     * @param ui the UI used to display the result
     */
    private static void addEvent(List<Task> tasks, String command, Ui ui) throws CaitlynException {
        int fromMarkerIndex = command.indexOf("/from");
        int toMarkerIndex = command.indexOf("/to");
        if (fromMarkerIndex <= 0 || toMarkerIndex <= fromMarkerIndex) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please provide an event in the format: event task /from start /to end.");
        }

        String description = command.substring(0, fromMarkerIndex).trim();
        String from = command.substring(fromMarkerIndex + "/from".length(), toMarkerIndex).trim();
        String to = command.substring(toMarkerIndex + "/to".length()).trim();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please provide a description, start time, and end time for the event.");
        }
        try {
            addTask(tasks, new Event(description, from, to), ui);
        } catch (IllegalArgumentException exception) {
            throw new CaitlynException(
                    "I beg your pardon, master. Please use valid dates such as 2019-10-15 or 2/12/2019 1800.");
        }
    }
}
