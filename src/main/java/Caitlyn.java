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

        List<Task> tasks = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

            System.out.println(separator);
            if ("bye".equals(command)) {
                System.out.println("     Farewell, master. It has been my pleasure to serve you.");
                System.out.println(separator);
                break;
            }

            if ("list".equals(command)) {
                System.out.println("     Here are the tasks in your list:");
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println("     " + (i + 1) + "." + tasks.get(i));
                }
            } else if (command.startsWith("mark ")) {
                String[] commandParts = command.split(" ");
                if (commandParts.length != 2) {
                    System.out.println("     I beg your pardon, master. Please provide a task number, for example: mark 2");
                } else {
                    try {
                        int taskNumber = Integer.parseInt(commandParts[1]);
                        if (taskNumber < 1 || taskNumber > tasks.size()) {
                            System.out.println("     I beg your pardon, master, but I could not find task " + taskNumber + ".");
                        } else {
                            Task task = tasks.get(taskNumber - 1);
                            task.markAsDone();
                            System.out.println("     As you wish, master. I have marked this task as done:");
                            System.out.println("       " + task);
                        }
                    } catch (NumberFormatException exception) {
                        System.out.println("     I beg your pardon, master. Please provide a valid task number, for example: mark 2");
                    }
                }
            } else if (command.startsWith("unmark ")) {
                String[] commandParts = command.split(" ");
                if (commandParts.length != 2) {
                    System.out.println("     I beg your pardon, master. Please provide a task number, for example: unmark 2");
                } else {
                    try {
                        int taskNumber = Integer.parseInt(commandParts[1]);
                        if (taskNumber < 1 || taskNumber > tasks.size()) {
                            System.out.println("     I beg your pardon, master, but I could not find task " + taskNumber + ".");
                        } else {
                            Task task = tasks.get(taskNumber - 1);
                            task.markAsNotDone();
                            System.out.println("     Of course, master. I have marked this task as not done yet:");
                            System.out.println("       " + task);
                        }
                    } catch (NumberFormatException exception) {
                        System.out.println("     I beg your pardon, master. Please provide a valid task number, for example: unmark 2");
                    }
                }
            } else if (command.startsWith("todo ")) {
                addTask(tasks, new Todo(command.substring("todo ".length()).trim()));
            } else if (command.startsWith("deadline ")) {
                addDeadline(tasks, command.substring("deadline ".length()).trim());
            } else if (command.startsWith("event ")) {
                addEvent(tasks, command.substring("event ".length()).trim());
            } else {
                // Keep accepting the old plain-text form as a ToDo for compatibility.
                addTask(tasks, new Todo(command));
            }

            System.out.println(separator);
        }
    }

    /**
     * Adds a task and prints the standard confirmation message.
     *
     * @param tasks the current task list
     * @param task the task to add
     */
    private static void addTask(List<Task> tasks, Task task) {
        tasks.add(task);
        System.out.println("     Got it. I've added this task:");
        System.out.println("       " + task);
        System.out.println("     Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Parses and adds a deadline command.
     *
     * @param tasks the current task list
     * @param command the part of the command after {@code deadline}
     */
    private static void addDeadline(List<Task> tasks, String command) {
        int markerIndex = command.indexOf("/by");
        if (markerIndex <= 0) {
            System.out.println("     Please provide a deadline in the format: deadline task /by date");
            return;
        }

        String description = command.substring(0, markerIndex).trim();
        String by = command.substring(markerIndex + "/by".length()).trim();
        if (description.isEmpty() || by.isEmpty()) {
            System.out.println("     Please provide both a task description and a deadline.");
            return;
        }
        addTask(tasks, new Deadline(description, by));
    }

    /**
     * Parses and adds an event command.
     *
     * @param tasks the current task list
     * @param command the part of the command after {@code event}
     */
    private static void addEvent(List<Task> tasks, String command) {
        int fromMarkerIndex = command.indexOf("/from");
        int toMarkerIndex = command.indexOf("/to");
        if (fromMarkerIndex <= 0 || toMarkerIndex <= fromMarkerIndex) {
            System.out.println("     Please provide an event in the format: event task /from start /to end");
            return;
        }

        String description = command.substring(0, fromMarkerIndex).trim();
        String from = command.substring(fromMarkerIndex + "/from".length(), toMarkerIndex).trim();
        String to = command.substring(toMarkerIndex + "/to".length()).trim();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            System.out.println("     Please provide a description, start time, and end time for the event.");
            return;
        }
        addTask(tasks, new Event(description, from, to));
    }
}
