import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
        String banner = "  ____      _ _   _             \n"
                + " / ___|__ _(_) |_| |_   _ _ __  \n"
                + "| |   / _` | | __| | | | | '_ \\ \n"
                + "| |__| (_| | | |_| | |_| | | | |\n"
                + " \\____\\__,_|_|\\__|_|\\__, |_| |_|\n"
                + "                    |___/       \n";
        String separator = "____________________________________________________________";

        System.out.println(separator);
        System.out.println(banner);
        System.out.println("Good day, master. I am Caitlyn, humbly at your service.");
        System.out.println("How may I serve you today?");
        System.out.println(separator);

        List<Task> tasks = loadTasks();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

            System.out.println(separator);
            try {
                if ("bye".equals(command)) {
                    System.out.println("     Farewell, master. It has been my pleasure to serve you.");
                    System.out.println(separator);
                    break;
                }

                handleCommand(command, tasks);
            } catch (CaitlynException exception) {
                System.out.println("     " + exception.getMessage());
            }

            System.out.println(separator);
        }
    }

    /**
     * Loads the saved task list, falling back to an empty list when saved data cannot be read.
     *
     * @return the saved tasks or an empty task list
     */
    private static List<Task> loadTasks() {
        try {
            return TaskStorage.load();
        } catch (IOException | IllegalArgumentException exception) {
            System.out.println("     I could not read the saved tasks, so I am starting with an empty list.");
            return new ArrayList<>();
        }
    }

    /**
     * Interprets one user command and performs the requested action.
     *
     * @param command the trimmed command entered by the user
     * @param tasks the current task list
     * @throws CaitlynException when the command cannot be carried out
     */
    private static void handleCommand(String command, List<Task> tasks) throws CaitlynException {
        if ("list".equals(command)) {
            System.out.println("     Here are the tasks in your list:");
            for (int i = 0; i < tasks.size(); i++) {
                System.out.println("     " + (i + 1) + "." + tasks.get(i));
            }
        } else if (command.equals("mark") || command.startsWith("mark ")) {
            changeTaskStatus(command, tasks, true);
        } else if (command.equals("unmark") || command.startsWith("unmark ")) {
            changeTaskStatus(command, tasks, false);
        } else if (command.equals("delete") || command.startsWith("delete ")) {
            deleteTask(command, tasks);
        } else if (command.equals("todo") || command.startsWith("todo ")) {
            String description = command.substring("todo".length()).trim();
            if (description.isEmpty()) {
                throw new CaitlynException(
                        "I beg your pardon, master. I cannot prepare a task without a description.");
            }
            addTask(tasks, new Todo(description));
        } else if (command.equals("deadline") || command.startsWith("deadline ")) {
            addDeadline(tasks, command.substring("deadline".length()).trim());
        } else if (command.equals("event") || command.startsWith("event ")) {
            addEvent(tasks, command.substring("event".length()).trim());
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
     * @throws CaitlynException when the command has no valid task number
     */
    private static void changeTaskStatus(String command, List<Task> tasks, boolean markAsDone)
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
        if (markAsDone) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
        saveTasks(tasks);
        if (markAsDone) {
            System.out.println("     As you wish, master. I have marked this task as done:");
        } else {
            System.out.println("     Of course, master. I have marked this task as not done yet:");
        }
        System.out.println("       " + task);
    }

    /**
     * Deletes a task after validating the task number and reports the updated task count.
     *
     * @param command the complete delete command
     * @param tasks the current task list
     * @throws CaitlynException when the command has no valid task number
     */
    private static void deleteTask(String command, List<Task> tasks) throws CaitlynException {
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
        saveTasks(tasks);
        System.out.println("     Noted. I've removed this task:");
        System.out.println("       " + removedTask);
        System.out.println("     Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Adds a task and prints the standard confirmation message.
     *
     * @param tasks the current task list
     * @param task the task to add
     */
    private static void addTask(List<Task> tasks, Task task) throws CaitlynException {
        tasks.add(task);
        saveTasks(tasks);
        System.out.println("     Got it. I've added this task:");
        System.out.println("       " + task);
        System.out.println("     Now you have " + tasks.size() + " tasks in the list.");
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
        } catch (IOException exception) {
            throw new CaitlynException(
                    "I beg your pardon, master. I could not save your tasks to disk.");
        }
    }

    /**
     * Parses and adds a deadline command.
     *
     * @param tasks the current task list
     * @param command the part of the command after {@code deadline}
     */
    private static void addDeadline(List<Task> tasks, String command) throws CaitlynException {
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
        addTask(tasks, new Deadline(description, by));
    }

    /**
     * Parses and adds an event command.
     *
     * @param tasks the current task list
     * @param command the part of the command after {@code event}
     */
    private static void addEvent(List<Task> tasks, String command) throws CaitlynException {
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
        addTask(tasks, new Event(description, from, to));
    }
}
