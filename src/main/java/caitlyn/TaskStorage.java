package caitlyn;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
     * @param tasks the tasks to save.
     * @throws IOException if the directory or file cannot be written.
     */
    public static void save(List<Task> tasks) throws IOException {
        if (tasks == null) {
            throw new IllegalArgumentException("The task list cannot be null.");
        }
        Files.createDirectories(TASK_FILE.getParent());
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            if (task == null) {
                throw new IllegalArgumentException("The task list cannot contain null tasks.");
            }
            lines.add(task.toStorageString());
        }
        Path temporaryFile = Files.createTempFile(
                TASK_FILE.getParent(), TASK_FILE.getFileName().toString(), ".tmp");
        boolean hasMoved = false;
        try {
            Files.write(
                    temporaryFile,
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            try {
                Files.move(
                        temporaryFile,
                        TASK_FILE,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
                hasMoved = true;
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, TASK_FILE, StandardCopyOption.REPLACE_EXISTING);
                hasMoved = true;
            }
        } finally {
            if (!hasMoved) {
                Files.deleteIfExists(temporaryFile);
            }
        }
    }

    /**
     * Loads all saved tasks from the local data file.
     *
     * @return the saved tasks, or an empty list when no data file exists.
     * @throws IOException if the data file cannot be read.
     * @throws IllegalArgumentException if a saved line has an invalid format.
     */
    public static List<Task> load() throws IOException {
        if (Files.notExists(TASK_FILE)) {
            return new ArrayList<>();
        }

        List<Task> tasks = new ArrayList<>();
        List<String> lines = Files.readAllLines(TASK_FILE, StandardCharsets.UTF_8);
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (!line.isBlank()) {
                try {
                    tasks.add(parseTask(line));
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException(
                            "Invalid saved task on line " + (index + 1) + ": " + exception.getMessage(),
                            exception);
                }
            }
        }
        return tasks;
    }

    /**
     * Converts one saved line into a task object.
     *
     * @param line the pipe-separated task line.
     * @return the task represented by the line.
     * @throws IllegalArgumentException if the line is not valid storage data.
     */
    private static Task parseTask(String line) {
        List<String> fields = splitFields(line);
        if (fields.size() < 3) {
            throw new IllegalArgumentException("A saved task must have a type, status, and description.");
        }

        boolean isDone = switch (fields.get(1)) {
        case "0" -> false;
        case "1" -> true;
        default -> throw new IllegalArgumentException("A saved task status must be 0 or 1.");
        };

        Task task;
        switch (fields.get(0)) {
        case "T":
            requireFieldCount(fields, 3);
            task = new Todo(fields.get(2));
            break;
        case "D":
            requireFieldCount(fields, 4);
            task = new Deadline(fields.get(2), fields.get(3));
            break;
        case "E":
            requireFieldCount(fields, 5);
            task = new Event(fields.get(2), fields.get(3), fields.get(4));
            break;
        default:
            throw new IllegalArgumentException("Unknown saved task type: " + fields.get(0));
        }

        if (isDone) {
            task.markAsDone();
        }
        return task;
    }

    /**
     * Ensures a parsed task has exactly the fields required by its type.
     *
     * @param fields the fields parsed from a saved line.
     * @param expectedCount the number of fields required by the task type.
     * @throws IllegalArgumentException if there are too few or too many fields.
     */
    private static void requireFieldCount(List<String> fields, int expectedCount) {
        if (fields.size() != expectedCount) {
            throw new IllegalArgumentException("The saved task has the wrong number of fields.");
        }
    }

    /**
     * Splits a saved line while treating escaped pipes as part of a field.
     *
     * @param line the saved task line.
     * @return decoded and trimmed fields.
     * @throws IllegalArgumentException if the line ends with an incomplete escape.
     */
    private static List<String> splitFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '|') {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else if (character == '\\') {
                if (index + 1 >= line.length()) {
                    throw new IllegalArgumentException("A saved task has an incomplete escape sequence.");
                }
                char escapedCharacter = line.charAt(++index);
                switch (escapedCharacter) {
                case '\\' -> field.append('\\');
                case '|' -> field.append('|');
                case 'n' -> field.append('\n');
                case 'r' -> field.append('\r');
                default -> field.append('\\').append(escapedCharacter);
                }
            } else {
                field.append(character);
            }
        }
        fields.add(field.toString().trim());
        return fields;
    }
}
