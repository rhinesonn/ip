package caitlyn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Shares task loading and protection after a load failure between the CLI and GUI. */
public final class TaskSession {
    /** The tasks loaded for this session, or an empty list after loading fails. */
    private final List<Task> tasks;

    /** Whether changes are blocked for the lifetime of this session. */
    private final boolean hasLoadingError;

    /** Creates a session, preserving unreadable data by blocking subsequent changes. */
    public TaskSession() {
        List<Task> loadedTasks;
        boolean loadingFailed = false;
        try {
            loadedTasks = TaskStorage.load();
        } catch (IOException | IllegalArgumentException | SecurityException exception) {
            loadedTasks = new ArrayList<>();
            loadingFailed = true;
        }
        tasks = loadedTasks;
        hasLoadingError = loadingFailed;
    }

    /**
     * Executes a command only when it cannot overwrite data after a load failure.
     *
     * @param command the parsed command to execute.
     * @param ui the destination for command responses.
     * @throws CaitlynException if changes are blocked or the command fails.
     */
    public void execute(Command command, Ui ui) throws CaitlynException {
        if (hasLoadingError && !command.isReadOnly()) {
            throw new CaitlynException("I beg your pardon, master. Task changes are disabled because "
                    + "saved tasks could not be loaded. Repair data/duke.txt or its access permissions, "
                    + "then restart Caitlyn.");
        }
        command.execute(tasks, ui);
    }

    /**
     * Returns whether this session could not load its saved tasks.
     *
     * @return whether task changes must remain blocked until a successful restart.
     */
    public boolean hasLoadingError() {
        return hasLoadingError;
    }

    /**
     * Returns the number of tasks currently held in memory.
     *
     * @return the current task count.
     */
    public int getTaskCount() {
        return tasks.size();
    }
}
