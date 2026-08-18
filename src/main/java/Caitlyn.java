/**
 * Entry point for the chatbot application.
 */
public class Caitlyn {
    /**
     * Starts the application, greets the user, and exits.
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
        System.out.println("Bye Master. Please let me know if you need anything else!");
        System.out.println(separator);
    }
}
