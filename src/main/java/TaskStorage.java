import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes Caitlyn's task list in the local data file.
 */
public final class TaskStorage {
    /** The relative path where the task list is stored. */
    private static final Path TASK_FILE = Path.of("data", "duke.txt");

    /** Prevents construction of this utility class. */
    private TaskStorage() {
    }

    /**
     * Replaces the saved task list with the current tasks.
     *
     * @param tasks the tasks to save
     * @throws IOException if the directory or file cannot be written
     */
    public static void save(List<Task> tasks) throws IOException {
        Files.createDirectories(TASK_FILE.getParent());
        List<String> lines = tasks.stream()
                .map(Task::toStorageString)
                .toList();
        Files.write(
                TASK_FILE,
                lines,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }

    /**
     * Loads all saved tasks from the local data file.
     *
     * @return the saved tasks, or an empty list when no data file exists
     * @throws IOException if the data file cannot be read
     * @throws IllegalArgumentException if a saved line has an invalid format
     */
    public static List<Task> load() throws IOException {
        if (!Files.exists(TASK_FILE)) {
            return new ArrayList<>();
        }

        List<Task> tasks = new ArrayList<>();
        for (String line : Files.readAllLines(TASK_FILE, StandardCharsets.UTF_8)) {
            if (!line.isBlank()) {
                tasks.add(parseTask(line));
            }
        }
        return tasks;
    }

    /**
     * Converts one saved line into a task object.
     *
     * @param line the pipe-separated task line
     * @return the task represented by the line
     * @throws IllegalArgumentException if the line is not valid storage data
     */
    private static Task parseTask(String line) {
        String[] fields = line.trim().split("\\s*\\|\\s*", -1);
        if (fields.length < 3) {
            throw new IllegalArgumentException("A saved task must have a type, status, and description.");
        }

        boolean isDone = switch (fields[1]) {
        case "0" -> false;
        case "1" -> true;
        default -> throw new IllegalArgumentException("A saved task status must be 0 or 1.");
        };

        Task task;
        switch (fields[0]) {
        case "T":
            requireFieldCount(fields, 3);
            task = new Todo(fields[2]);
            break;
        case "D":
            requireFieldCount(fields, 4);
            task = new Deadline(fields[2], fields[3]);
            break;
        case "E":
            requireFieldCount(fields, 5);
            task = new Event(fields[2], fields[3], fields[4]);
            break;
        default:
            throw new IllegalArgumentException("Unknown saved task type: " + fields[0]);
        }

        if (isDone) {
            task.markAsDone();
        }
        return task;
    }

    /**
     * Ensures a parsed task has exactly the fields required by its type.
     *
     * @param fields the fields parsed from a saved line
     * @param expectedCount the number of fields required by the task type
     * @throws IllegalArgumentException if there are too few or too many fields
     */
    private static void requireFieldCount(String[] fields, int expectedCount) {
        if (fields.length != expectedCount) {
            throw new IllegalArgumentException("The saved task has the wrong number of fields.");
        }
    }

}
