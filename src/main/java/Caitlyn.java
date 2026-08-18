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

        List<String> tasks = new ArrayList<>();
        List<Boolean> taskStatuses = new ArrayList<>();
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
                System.out.println("     Of course, master. Here are the tasks in your list:");
                for (int i = 0; i < tasks.size(); i++) {
                    String status = taskStatuses.get(i) ? "X" : " ";
                    System.out.println("     " + (i + 1) + ".[" + status + "] " + tasks.get(i));
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
                            taskStatuses.set(taskNumber - 1, true);
                            System.out.println("     As you wish, master. I have marked this task as done:");
                            System.out.println("       [X] " + tasks.get(taskNumber - 1));
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
                            taskStatuses.set(taskNumber - 1, false);
                            System.out.println("     Of course, master. I have marked this task as not done yet:");
                            System.out.println("       [ ] " + tasks.get(taskNumber - 1));
                        }
                    } catch (NumberFormatException exception) {
                        System.out.println("     I beg your pardon, master. Please provide a valid task number, for example: unmark 2");
                    }
                }
            } else {
                tasks.add(command);
                taskStatuses.add(false);
                System.out.println("     As you wish, master. I have added: " + command);
            }

            System.out.println(separator);
        }
    }
}
