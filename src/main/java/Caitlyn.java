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
        System.out.println("Hello! I'm Caitlyn.");
        System.out.println("What can I do for you, master?");
        System.out.println(separator);

        List<String> tasks = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();

            System.out.println(separator);
            if ("bye".equals(command)) {
                System.out.println("     Bye, master. I hope to serve you again soon!");
                System.out.println(separator);
                break;
            }

            if ("list".equals(command)) {
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println((i + 1) + ". " + tasks.get(i));
                }
            } else {
                tasks.add(command);
                System.out.println("     added: " + command);
            }

            System.out.println(separator);
        }
    }
}
