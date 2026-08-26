import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Writes Caitlyn's task list to the local data file.
 *
 * <p>Reading is intentionally not part of this first persistence increment.</p>
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
}
