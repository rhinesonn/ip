package caitlyn;

import java.util.List;
import java.util.Scanner;

/**
 * Handles Caitlyn's command-line input and output.
 */
public final class Ui {
    /** The separator printed around the application's conversations. */
    private static final String SEPARATOR = "____________________________________________________________";

    /** The banner shown when Caitlyn starts. */
    private static final String BANNER = "  ____      _ _   _             \n"
            + " / ___|__ _(_) |_| |_   _ _ __  \n"
            + "| |   / _` | | __| | | | | '_ \\ \n"
            + "| |__| (_| | | |_| | |_| | | | |\n"
            + " \\____\\__,_|_|\\__|_|\\__, |_| |_|\n"
            + "                    |___/       \n";

    /** Source of commands entered by the user. */
    private final Scanner scanner;

    /** Creates a UI connected to the standard input and output streams. */
    public Ui() {
        this(new Scanner(System.in));
    }

    /**
     * Creates a UI with a supplied input source.
     *
     * @param scanner source from which commands are read
     */
    Ui(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("The UI scanner cannot be null.");
        }
        this.scanner = scanner;
    }

    /** Displays Caitlyn's greeting. */
    public void showWelcome() {
        showSeparator();
        System.out.println(BANNER);
        System.out.println("Good day, master. I am Caitlyn, humbly at your service.");
        System.out.println("How may I serve you today?");
        showSeparator();
    }

    /** Returns whether another command is available. */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /** Reads and trims the next command. */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /** Displays the standard conversation separator. */
    public void showSeparator() {
        System.out.println(SEPARATOR);
    }

    /** Displays Caitlyn's farewell message. */
    public void showFarewell() {
        System.out.println("     Farewell, master. It has been my pleasure to serve you.");
    }

    /** Displays an error encountered while loading saved tasks. */
    public void showLoadingError() {
        showError("I could not read the saved tasks, so I am starting with an empty list.");
    }

    /** Displays a user-facing command error. */
    public void showError(String message) {
        System.out.println("     " + message);
    }

    /** Displays all tasks with their one-based list numbers. */
    public void showTasks(List<Task> tasks) {
        System.out.println("     Here are the tasks in your list:");
        for (int index = 0; index < tasks.size(); index++) {
            System.out.println("     " + (index + 1) + "." + tasks.get(index));
        }
    }

    /** Displays matching tasks while retaining their original list numbers. */
    public void showMatchingTasks(List<Task> tasks, List<Task> matchingTasks) {
        System.out.println("     Here are the matching tasks in your list:");
        for (int index = 0; index < tasks.size(); index++) {
            if (matchingTasks.contains(tasks.get(index))) {
                System.out.println("     " + (index + 1) + "." + tasks.get(index));
            }
        }
    }

    /** Displays confirmation after a task is added. */
    public void showTaskAdded(Task task, int taskCount) {
        System.out.println("     Got it. I've added this task:");
        System.out.println("       " + task);
        System.out.println("     Now you have " + taskCount + " tasks in the list.");
    }

    /** Displays confirmation after a task is deleted. */
    public void showTaskDeleted(Task task, int taskCount) {
        System.out.println("     Noted. I've removed this task:");
        System.out.println("       " + task);
        System.out.println("     Now you have " + taskCount + " tasks in the list.");
    }

    /** Displays confirmation after a task's completion status changes. */
    public void showTaskStatus(Task task, boolean markAsDone) {
        if (markAsDone) {
            System.out.println("     As you wish, master. I have marked this task as done:");
        } else {
            System.out.println("     Of course, master. I have marked this task as not done yet:");
        }
        System.out.println("       " + task);
    }
}
